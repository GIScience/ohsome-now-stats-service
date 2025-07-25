package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.statsSchemaVersion
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
import java.time.temporal.ChronoUnit
import javax.sql.DataSource


@Suppress("LargeClass")
@Component
class StatsRepo {

    @Autowired
    lateinit var dataSource: DataSource

    private val logger: Logger = LoggerFactory.getLogger(StatsRepo::class.java)


    fun defaultResultForMissingUser(userId: String): MutableMap<String, Any> = mutableMapOf(
        "user_id" to userId.toInt(),
        "contributor" to 1,
        "edit" to UnsignedLong.valueOf(0),
        "changeset" to UnsignedLong.valueOf(0)
    )

    @Deprecated("remove together with controller")
    //language=sql
    fun statsFromTimeSpanSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            ${hashtagHandler.optionalFilterSQL}        
            changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
            ;
        """.trimIndent()

    //language=sql
    fun statsFromTimeSpanSQL(
        hashtagHandler: HashtagHandler,
        countryHandler: CountryHandler,
        statsTopicHandler: StatsTopicsHandler
    ) = """
        SELECT
            ${statsTopicHandler.statsTopicSQL}
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            ${hashtagHandler.optionalFilterSQL}        
            changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
            ;
        """.trimIndent()


    //language=sql
    private fun statsFromTimeSpanAggregateSQL(hashtagHandler: HashtagHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest,
            arrayJoin(arrayFilter(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)) as hashtag
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            has_hashtags = true
            AND arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
            AND changeset_timestamp > parseDateTimeBestEffort(:startDate) 
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY hashtag
        ORDER BY hashtag
        """.trimIndent()

    @Deprecated("Remove with controller")
    @Suppress("LongMethod")
    //language=sql
    fun statsFromTimeSpanIntervalSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
    SELECT
        groupArray(changesets)as changesets,	
        groupArray(users)as users,
        groupArray(edits)as edits,
        groupArray(inner_startdate)as startdate,
        groupArray(inner_startdate + INTERVAL :interval)as enddate
    FROM
    (    
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            count(map_feature_edit) as edits,
            toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as inner_startdate
        FROM "all_stats_$statsSchemaVersion"
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
    ;
    """.trimIndent()

    @Suppress("LongMethod")
    //language=sql
    fun statsFromTimeSpanIntervalSQL(
        hashtagHandler: HashtagHandler,
        countryHandler: CountryHandler,
        statsTopicHandler: StatsTopicsHandler
    ) = """
    SELECT
        ${statsTopicHandler.statsTopicAggregationSQL},       
        groupArray(inner_startdate) as startdate,
        groupArray(inner_startdate + INTERVAL :interval) as enddate
    FROM
    (    
        SELECT
            ${statsTopicHandler.statsTopicSQL},
            toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as inner_startdate
        FROM "all_stats_$statsSchemaVersion"
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
    ;
    """.trimIndent()

    @Deprecated("remove with controller")
    @Suppress("LongMethod")
    //language=sql
    fun statsFromTimeSpanCountrySQL(hashtagHandler: HashtagHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest,
            country_iso_a3 as country
            FROM "all_stats_$statsSchemaVersion"
        ARRAY JOIN country_iso_a3
        WHERE
            ${hashtagHandler.optionalFilterSQL}
            changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY
            country
        ORDER BY country
        ;
        """.trimIndent()

    @Suppress("LongMethod")
    //language=sql
    fun statsFromTimeSpanCountrySQL(hashtagHandler: HashtagHandler, statsTopicHandler: StatsTopicsHandler) = """
        SELECT
            ${statsTopicHandler.statsTopicSQL},
            country_iso_a3 as country
        FROM "all_stats_$statsSchemaVersion"
        ARRAY JOIN country_iso_a3
        WHERE
            ${hashtagHandler.optionalFilterSQL}
            changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY
            country
        ORDER BY country
        ;
        """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    private fun statsFromH3SQL(
        hashtagHandler: HashtagHandler,
        statsTopicHandler: StatsTopicsHandler,
        countryHandler: CountryHandler
    ) = """
        SELECT
            ${statsTopicHandler.statsTopicSQL},
            h3ToString(h3_r:resolution) as hex
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            ${hashtagHandler.optionalFilterSQL}
            changeset_timestamp > parseDateTimeBestEffort(:startDate)
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
            AND isNotNull(h3_r:resolution) 
            ${countryHandler.optionalFilterSQL}
        GROUP BY hex
        FORMAT CSV
    """.trimIndent()


    //language=sql
    fun statsByUserIdSQL(hashtagHandler: HashtagHandler, statsTopicsHandler: StatsTopicsHandler) = """
        SELECT
            ${statsTopicsHandler.statsTopicSQL},
            user_id
        FROM "all_stats_user_$statsSchemaVersion"
        WHERE
            ${hashtagHandler.optionalFilterSQL}
            user_id = :userId 
            AND changeset_timestamp > parseDateTimeBestEffort(:startDate) 
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY user_id
        ;
    """.trimIndent()


    //language=sql
    private fun mostUsedHashtagsSQL(countryHandler: CountryHandler) = """
        SELECT 
            arrayJoin(hashtags) as hashtag, 
            COUNT(DISTINCT user_id) as number_of_users
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            has_hashtags = true
            AND changeset_timestamp > parseDateTimeBestEffort(:startDate) 
            AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        GROUP BY
            hashtag
        ORDER BY
            number_of_users DESC
        LIMIT :limit
    """.trimIndent()


    //language=sql
    private val metadataSQL = """
        SELECT 
            max(changeset_timestamp) as max_timestamp,
            min(changeset_timestamp) as min_timestamp
        FROM "all_stats_$statsSchemaVersion"
        ;
    """.trimIndent()


    private val uniqueHashtagSQL = """
        SELECT
            hashtag,
            count
        FROM "hashtag_aggregation_$statsSchemaVersion"
        WHERE
            count > 10
            AND hashtag not like '% %'
            AND hashtag not like '%﻿%'
            AND not match(hashtag, '^[0-9·-]*$')
        ORDER BY hashtag
        ;
    """.trimIndent()

    /**
     * Retrieves statistics for a specific hashtag within a time span.
     *
     * @param hashtagHandler Contains the hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A map containing the statistics.
     */
    @Deprecated("Remove with controller")
    fun getStatsForTimeSpan(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler
    ): Map<String, Any> {
        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate")

        val result = query {
            it.select(statsFromTimeSpanSQL(hashtagHandler, countryHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .single()
        }

        return result + ("hashtag" to hashtagHandler.hashtag)
    }

    @Suppress("LongParameterList")
            /**
             * Retrieves statistics for a specific hashtag within a time span.
             *
             * @param hashtagHandler Contains the hashtag to retrieve statistics for.
             * @param startDate The start date of the time span.
             * @param endDate The end date of the time span.
             * @param countryHandler handles the filtering of countries in the WHERE.
             * @param statsTopicHandler handles which stats are calculated in the SELECT.
             * @return A map containing the statistics.
             */
    fun getStatsForTimeSpan(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler,
        statsTopicHandler: StatsTopicsHandler
    ): Map<String, Any> {
        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate")

        val result = query {
            it.select(statsFromTimeSpanSQL(hashtagHandler, countryHandler, statsTopicHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .single()
        }

        return result + ("hashtag" to hashtagHandler.hashtag)
    }


    /**
     * Retrieves statistics for a specific hashtag within a time span.
     *
     * @param hashtagHandler Contains the hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A map containing the statistics.
     */
    fun getStatsForTimeSpanAggregate(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?
    ): List<MutableMap<String, Any>> {
        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate")

        return query {
            it.select(statsFromTimeSpanAggregateSQL(hashtagHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .list()
        }
    }


    /**
     * Retrieves statistics for a specific hashtag within a time span and interval.
     *
     * @param hashtagHandler Contains the hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @param interval The interval for grouping the statistics.
     * @return A list of maps containing the statistics for each interval.
     */
    @Deprecated("remove with controller")
    @Suppress("LongParameterList")
    fun getStatsForTimeSpanInterval(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countryHandler: CountryHandler
    ): Map<String, Any> {

        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval")

        return query {
            it.select(statsFromTimeSpanIntervalSQL(hashtagHandler, countryHandler))
                .bind("interval", getGroupbyInterval(interval))
                .bind("startdate", startDate ?: EPOCH)
                .bind("enddate", endDate ?: now())
                .bind("hashtag", hashtagHandler.hashtag)
                .mapToMap()
                .single()
        }
    }

    /**
     * Retrieves statistics for a specific hashtag within a time span and interval.
     *
     * @param hashtagHandler Contains the hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @param interval The interval for grouping the statistics.
     * @param countryHandler handles the filtering of countries in the WHERE.
     * @param statsTopicHandler handles which stats are calculated in the SELECT.
     * @return A list of maps containing the statistics for each interval.
     */
    @Suppress("LongParameterList")
    fun getStatsForTimeSpanInterval(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countryHandler: CountryHandler,
        statsTopicHandler: StatsTopicsHandler
    ): Map<String, Any> {

        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval")

        return query {
            it.select(statsFromTimeSpanIntervalSQL(hashtagHandler, countryHandler, statsTopicHandler))
                .bind("interval", getGroupbyInterval(interval))
                .bind("startdate", startDate ?: EPOCH)
                .bind("enddate", endDate ?: now())
                .bind("hashtag", hashtagHandler.hashtag)
                .mapToMap()
                .single()
        }
    }


    /**
     * Retrieves aggregated HOT-TM-project statistics for a specific user.
     * ATTENTION: EXPOSING THIS QUERY MIGHT VIOLATE DATA PRIVACY
     * @param userId the osm userid which should be queried.
     * @return A list of maps containing the statistics for all hotTM projects.
     */
    @Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
    fun getStatsByUserId(
        userId: String,
        hashtagHandler: HashtagHandler,
        statsTopicsHandler: StatsTopicsHandler,
        startDate: Instant?,
        endDate: Instant?
    ): MutableMap<String, Any> {
        logger.info("Getting HotOSM stats for user: $userId")

        return query {
            it.select(statsByUserIdSQL(hashtagHandler, statsTopicsHandler))
                .bind("userId", userId)
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .singleOrNull()
                ?: defaultResultForMissingUser(userId)
        }
    }


    /**
     * Retrieves statistics for a specific hashtag grouped by country.
     *
     * @param hashtagHandler The hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A list of maps containing the statistics for each interval.
     */
    @Deprecated("remove with controller")
    fun getStatsForTimeSpanCountry(
        hashtagHandler: HashtagHandler,
        startDate: Instant? = EPOCH,
        endDate: Instant? = now()
    ): List<Map<String, Any>> {

        return query {
            it.select(statsFromTimeSpanCountrySQL(hashtagHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .list()
        }

    }

    fun getStatsForTimeSpanCountry(
        hashtagHandler: HashtagHandler,
        startDate: Instant? = EPOCH,
        endDate: Instant? = now(),
        statsTopicHandler: StatsTopicsHandler
    ): List<Map<String, Any>> {

        return query {
            it.select(statsFromTimeSpanCountrySQL(hashtagHandler, statsTopicHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .list()
        }

    }

    @Suppress("LongParameterList")
    fun getStatsByH3(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        statsTopicHandler: StatsTopicsHandler,
        resolution: Int,
        countryHandler: CountryHandler,
    ): String {
        return "result,hex_cell\n" + query {
            it.createQuery(statsFromH3SQL(hashtagHandler, statsTopicHandler, countryHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .bind("resolution", resolution)
                .mapTo(String::class.java)
                .reduce { a, b -> "$a\n$b" }
        }
    }


    /**
     * Retrieves the most used Hashtags in the selected Timeperiod.
     *
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A list of maps containing the statistics for each interval.
     */
    fun getMostUsedHashtags(
        startDate: Instant? = EPOCH,
        endDate: Instant? = now().truncatedTo(ChronoUnit.SECONDS),
        limit: Int? = 10,
        countryHandler: CountryHandler
    ): List<Map<String, Any>> {

        logger.info("Getting trending hashtags startDate: $startDate, endDate: $endDate, limit: $limit")

        return query {
            it.select(mostUsedHashtagsSQL(countryHandler))
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .bind("limit", limit)
                .mapToMap()
                .list()
        }

    }

    fun getUniqueHashtags(): List<Map<String, Any>> {
        return query {
            it.select(uniqueHashtagSQL).mapToMap().list()
        }
    }

    /**
     * Get min_timestamp and max_timestamp for the entire database.
     *
     * @return  A map containing the two keys.
     */
    fun getMetadata() = query {
        it.select(metadataSQL)
            .mapToMap()
            .single()
    }


    private fun <T> query(queryFunction: (handle: Handle) -> T) = create(dataSource)
        .withHandle<T, RuntimeException>(queryFunction)
}
