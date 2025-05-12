package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topicSchemaVersion
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

@Suppress("LargeClass")
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

        SELECT ${topicHandler.topicResult()}
        
        FROM topic_${topicHandler.topic}_$topicSchemaVersion
        WHERE
            has_hashtags = true
            AND arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
            AND changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        ;
        """.trimIndent()


    @Suppress("LongMethod")
    fun topicStatsFromTimeSpanAggregateSQL(
        hashtagHandler: HashtagHandler,
        topicHandler: TopicHandler
    ) = """
        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit

        SELECT 
            ${topicHandler.topicResult()}, 
            arrayJoin(arrayFilter(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)) as hashtag

        FROM topic_${topicHandler.topic}_$topicSchemaVersion
        WHERE
            has_hashtags = true
            AND arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
            AND changeset_timestamp > parseDateTimeBestEffort(:startDate) 
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY hashtag
        ORDER BY hashtag
    """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    fun topicStatsFromTimeSpanIntervalSQL(
        hashtagHandler: HashtagHandler,
        countryHandler: CountryHandler,
        topicHandler: TopicHandler
    ) = """
    SELECT
        ${topicHandler.topicArrayResult()}
        groupArray(inner_startdate) as startdate,
        groupArray(inner_startdate + INTERVAL :interval) as enddate
    FROM
    (
        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit
            
       SELECT 
           ${topicHandler.topicResult()},
           toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as inner_startdate

       FROM topic_${topicHandler.topic}_$topicSchemaVersion
       WHERE
           ${hashtagHandler.optionalFilterSQL}
           changeset_timestamp > parseDateTimeBestEffort(:startdate)
           AND changeset_timestamp < parseDateTimeBestEffort(:enddate)
           ${countryHandler.optionalFilterSQL}
       GROUP BY
           inner_startdate
       ORDER BY inner_startdate ASC
       WITH FILL
           FROM toStartOfInterval(parseDateTimeBestEffort(:startdate), INTERVAL :interval)::DateTime
           TO (toStartOfInterval(parseDateTimeBestEffort(:enddate), INTERVAL :interval)::DateTime + INTERVAL :interval)
       STEP INTERVAL :interval
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
            ${topicHandler.topicResult()},
            country_iso_a3 as country

        FROM topic_${topicHandler.topic}_$topicSchemaVersion
        ARRAY JOIN country_iso_a3
        WHERE
            ${hashtagHandler.optionalFilterSQL}
            changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY
            country
        ORDER BY
            country
        """.trimIndent()

    @Suppress("LongMethod")
    fun topicByUserIdSQL(topicHandler: TopicHandler, hashtagHandler: HashtagHandler) =
        """
        WITH
            ${topicHandler.valueLists()} 
            
            ${topicHandler.beforeCurrent()} 
            if ((current = 0) AND (before = 0), NULL, current - before) as edit
            
        SELECT 
            ${topicHandler.topicResult()},
        user_id
        FROM topic_${topicHandler.topic}_$topicSchemaVersion
        WHERE
            has_hashtags = true
            AND user_id = :userId
            AND arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
        GROUP BY user_id
        """


    fun defaultTopicResultForMissingUser(userId: String, topicAggregationType: String): Map<String, Any> = mapOf(
        "topic_result" to 0.0,
        "topic_result_created" to 0.0,
        "topic_result_modified" to 0L,
        "topic_result_deleted" to 0.0,
        "topic_result_modified_more" to 0.0,
        "topic_result_modified_less" to 0.0,
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
    ): Map<String, Any> {

        logger.info("Getting topic stats by interval for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval, topic: ${topicHandler.topic}")

        val result = query {
            it.select(topicStatsFromTimeSpanIntervalSQL(hashtagHandler, countryHandler, topicHandler))
                .bind("interval", getGroupbyInterval(interval))
                .bind("startdate", startDate ?: EPOCH)
                .bind("enddate", endDate ?: now())
                .bind("hashtag", hashtagHandler.hashtag)
                .mapToMap()
                .single()
        }
        println(result)
        return result
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

    fun getTopicbyUserId(
        userId: String,
        topicHandler: TopicHandler,
        hashtagHandler: HashtagHandler
    ): Map<String, Any> {
        logger.info("Getting topic stats for user: $userId, topic: ${topicHandler.topic}")
        return query {
            it.select(topicByUserIdSQL(topicHandler, hashtagHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("userId", userId)
                .mapToMap()
                .singleOrNull()
                ?: defaultTopicResultForMissingUser(userId, topicHandler.definition.aggregationStrategy.toString())
        }
    }


    private fun <T> query(queryFunction: (handle: Handle) -> T) = create(dataSource)
        .withHandle<T, RuntimeException>(queryFunction)

    fun getTopicStatsForTimeSpanAggregate(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        topicHandler: TopicHandler
    ): List<Map<String, Any>> {
        logger.info("Getting aggregated topic stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, topic: ${topicHandler.topic}")

        return query {
            it.select(topicStatsFromTimeSpanAggregateSQL(hashtagHandler, topicHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .list()
        }
    }
}
