package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.getGroupbyInterval
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi.create
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Instant.EPOCH
import java.time.Instant.now
import javax.sql.DataSource


@Component
class TopicRepo {

    @Autowired
    lateinit var dataSource: DataSource

    private val logger: Logger = LoggerFactory.getLogger(TopicRepo::class.java)


    @Suppress("LongMethod")
    //language=sql
    private fun topicStatsFromTimeSpanSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
        WITH
            ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 
            'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags, 
            
            place_current in place_tags as current, 
            place_before in place_tags as before, 
            if ((current = 0) AND (before = 0), NULL, current - before) as place_edit

        SELECT sum(place_edit) as topic_result

        FROM topic_place
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag) 
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        ;
        """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    private fun topicStatsFromTimeSpanIntervalSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """

        WITH
        ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 
        'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags, 
        
        place_current in place_tags as current, 
        place_before in place_tags as before, 
        if ((current = 0) AND (before = 0), NULL, current - before) as place_edit
            
       SELECT 
           ifNull(sum(place_edit), 0) as topic_result,
           toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as startdate,
           (toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime + INTERVAL :interval) as enddate

       FROM topic_place
       WHERE
           ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag)
           AND changeset_timestamp > parseDateTimeBestEffort(:startdate)
           AND changeset_timestamp < parseDateTimeBestEffort(:enddate)
           ${countryHandler.optionalFilterSQL}
       GROUP BY
           startdate
       ORDER BY startdate ASC
       WITH FILL
           FROM toStartOfInterval(parseDateTimeBestEffort(:startdate), INTERVAL :interval)::DateTime
           TO toStartOfInterval(parseDateTimeBestEffort(:enddate), INTERVAL :interval)::DateTime
       STEP INTERVAL :interval Interpolate (
           enddate as (
               if (
                   startdate != parseDateTimeBestEffort('1970-01-01 00:00:00'), -- condition
                   ((startdate + INTERVAL :interval) + INTERVAL :interval), 			 -- then
                   (toStartOfInterval(parseDateTimeBestEffort(:startdate), INTERVAL :interval) + INTERVAL :interval) -- else
               )
           )
       )
        """.trimIndent()




    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpan(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler,
        topic: String
    ): Map<String, Any> {
        logger.info("Getting topic stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate")

        val result = query {
            it.select(topicStatsFromTimeSpanSQL(hashtagHandler, countryHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .single()
        }

        return result + ("hashtag" to hashtagHandler.hashtag)
    }


    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpanInterval(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countryHandler: CountryHandler
    ): List<Map<String, Any>> {

        logger.info("Getting topic stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval")

        return query {
            it.select(topicStatsFromTimeSpanIntervalSQL(hashtagHandler, countryHandler))
                .bind("interval", getGroupbyInterval(interval))
                .bind("startdate", startDate ?: EPOCH)
                .bind("enddate", endDate ?: now())
                .bind("hashtag", hashtagHandler.hashtag)
                .mapToMap()
                .list()
        }
    }




    //TODO: remove duplication with StatsRepo
    private fun <T> query(queryFunction: (handle: Handle) -> T) = create(dataSource)
        .withHandle<T, RuntimeException>(queryFunction)


}
