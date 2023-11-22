package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
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


    //language=sql
    private fun topicStatsFromTimeSpanSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
        WITH
        ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 
            'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags, 
            
            place_current in place_tags as current, 
            place_before in place_tags as before, 
            if ((current = 0) AND (before = 0), NULL, current - before) as place_edit

        SELECT sum(place_edit) as places

        FROM topic_place
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag) 
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        ;
        """.trimIndent()


    fun getTopicStatsForTimeSpan(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler
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


    //TODO: remove duplication with StatsRepo
    private fun <T> query(queryFunction: (handle: Handle) -> T) = create(dataSource)
        .withHandle<T, RuntimeException>(queryFunction)


}
