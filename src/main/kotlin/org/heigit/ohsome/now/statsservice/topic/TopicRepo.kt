package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.schemaVersion
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.heigit.ohsome.now.statsservice.utils.getGroupbyInterval
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
    fun topicStatsFromTimeSpanSQL(
        hashtagHandler: HashtagHandler,
        countryHandler: CountryHandler,
        topicHandler: TopicHandler
    ) = """
        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit

        SELECT ${topicHandler.topicResult()} as topic_result

        FROM topic_${topicHandler.topic}_$schemaVersion
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag) 
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        ;
        """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    fun topicStatsFromTimeSpanIntervalSQL(
        hashtagHandler: HashtagHandler,
        countryHandler: CountryHandler,
        topicHandler: TopicHandler
    ) = """

        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit
            
       SELECT 
           ${topicHandler.topicResult()} as topic_result,
           toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as startdate,
           (toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime + INTERVAL :interval) as enddate

       FROM topic_${topicHandler.topic}_$schemaVersion
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
           TO (toStartOfInterval(parseDateTimeBestEffort(:enddate), INTERVAL :interval)::DateTime + INTERVAL :interval)
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


    @Suppress("LongMethod")
    //language=sql
    fun topicStatsFromTimeSpanCountrySQL(
        hashtagHandler: HashtagHandler,
        topicHandler:
        TopicHandler
    ) = """
        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit
            
        SELECT 
            ${topicHandler.topicResult()} as topic_result,
            country_iso_a3 as country

        FROM topic_${topicHandler.topic}_$schemaVersion
        ARRAY JOIN country_iso_a3
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag)
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY
            country
        ORDER BY
            country
        """.trimIndent()

    fun topicForUserIdForHotOSMProjectSQL(topicHandler: TopicHandler) =
        """        
        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit
            
        SELECT 
            ${topicHandler.topicResult()} as topic_result,
        user_id
        FROM topic_${topicHandler.topic}_$schemaVersion
        WHERE
        user_id = :userId
                and startsWith(hashtag, 'hotosm-project-')
        group by user_id
        """


    fun defaultTopicResultForMissingUser(userId: String, topic: String): Map<String, Any> = mapOf(
        "topic_result" to 0L,
        "user_id" to userId.toInt(),
    )

    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpan(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler,
        topicHandler: TopicHandler
    ): Map<String, Any> {
        logger.info("Getting topic stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, topic: ${topicHandler.topic}")

        val result = query {
            it.select(topicStatsFromTimeSpanSQL(hashtagHandler, countryHandler, topicHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
//                .setSqlLogger(Slf4JSqlLogger(logger))
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
        countryHandler: CountryHandler,
        topicHandler: TopicHandler
    ): List<Map<String, Any>> {

        logger.info("Getting topic stats by interval for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval, , topic: ${topicHandler.topic}")

        return query {
            it.select(topicStatsFromTimeSpanIntervalSQL(hashtagHandler, countryHandler, topicHandler))
                .bind("interval", getGroupbyInterval(interval))
                .bind("startdate", startDate ?: EPOCH)
                .bind("enddate", endDate ?: now())
                .bind("hashtag", hashtagHandler.hashtag)
                .mapToMap()
                .list()
        }
    }


    fun getTopicStatsForTimeSpanCountry(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        topicHandler: TopicHandler
    ): List<Map<String, Any>> {

        logger.info("Getting topic stats by country for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, topic: ${topicHandler.topic}")

        return query {
            it.select(topicStatsFromTimeSpanCountrySQL(hashtagHandler, topicHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .list()
        }

    }

    fun getTopicForUserIdForAllHotTMProjects(userId: String, topicHandler: TopicHandler): Map<String, Any> {
        logger.info("Getting topic stats for user: $userId, topic: ${topicHandler.topic}")
        return query {
            it.select(topicForUserIdForHotOSMProjectSQL(topicHandler))
                .bind("userId", userId)
                .mapToMap()
                .singleOrNull()
                ?: defaultTopicResultForMissingUser(userId, topicHandler.topic)
        }
    }


    private fun <T> query(queryFunction: (handle: Handle) -> T) = create(dataSource)
        .withHandle<T, RuntimeException>(queryFunction)
}
