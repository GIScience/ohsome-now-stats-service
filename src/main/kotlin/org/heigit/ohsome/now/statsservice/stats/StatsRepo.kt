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


// TODO: find out where to use `has_hashtags=true` in SQL queries to speed up performance


@Suppress("LargeClass")
@Component
class StatsRepo {

    @Autowired
    lateinit var dataSource: DataSource

    private val logger: Logger = LoggerFactory.getLogger(StatsRepo::class.java)


    fun defaultResultForMissingUser(userId: String): MutableMap<String, Any> = mutableMapOf(
        "user_id" to userId.toInt(),
        "edits" to UnsignedLong.valueOf(0),
        "changesets" to UnsignedLong.valueOf(0)
    )


    //language=sql
    fun statsFromTimeSpanSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
        and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        """.trimIndent()


    //language=sql
    private fun statsFromTimeSpanAggregateSQL(hashtagHandler: HashtagHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest,
            arrayJoin(hashtags) as hashtag
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
            and changeset_timestamp > parseDateTimeBestEffort(:startDate) 
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY hashtag
        """.trimIndent()


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
            arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
            AND changeset_timestamp > parseDateTimeBestEffort(:startdate)
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
            arrayExists(hashtag -> ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag), hashtags)        
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY
            country
        ORDER BY country
        """.trimIndent()


    //language=sql
    private val statsForUserIdForHotOSMProjectSQL = """
        select
            count(map_feature_edit) as edits,
            count(distinct changeset_id) as changesets, 
            user_id
            FROM "all_stats_$statsSchemaVersion"
            WHERE
                user_id = :userId AND
                arrayExists(hashtag -> startsWith(hashtag, 'hotosm-project-'), hashtags)        
        group by user_id

    """.trimIndent()


    //language=sql
    private fun mostUsedHashtagsSQL(countryHandler: CountryHandler) = """
        SELECT 
            arrayJoin(hashtags) as hashtag, 
            COUNT(DISTINCT user_id) as number_of_users
        FROM "all_stats_$statsSchemaVersion"
        WHERE
            changeset_timestamp > parseDateTimeBestEffort(:startDate) and 
            changeset_timestamp < parseDateTimeBestEffort(:endDate)
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
        WHERE changeset_timestamp > now() - toIntervalMonth(1) 
        OR changeset_timestamp < parseDateTimeBestEffort('2009-04-23T00:00:00.000000Z')
    """.trimIndent()


    // TODO: should we port this to `arrayExists` with lambdas?
    private val uniqueHashtagSQL = """
        SELECT 
            arrayJoin(hashtags) as hashtag, 
            count(*) as count
        FROM "all_stats_$statsSchemaVersion"
        WHERE 
            hashtag not like '% %' 
            and hashtag not like '%﻿%' 
            and not match(hashtag, '^[0-9·-]*$')
        group by hashtag
        having count(*) > 10
        order by hashtag
    """.trimIndent()

    /**
     * Retrieves statistics for a specific hashtag within a time span.
     *
     * @param hashtagHandler Contains the hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A map containing the statistics.
     */
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
    ): List<Map<String, Any>> {
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
     * Retrieves aggregated HOT-TM-project statistics for a specific user.
     * ATTENTION: EXPOSING THIS QUERY MIGHT VIOLATE DATA PRIVACY
     * @param userId the osm userid which should be queried.
     * @return A list of maps containing the statistics for all hotTM projects.
     */
    @Suppress("LongMethod")
    fun getStatsForUserIdForAllHotTMProjects(
        userId: String
    ): MutableMap<String, Any> {
        logger.info("Getting HotOSM stats for user: ${userId}")

        return query {
            it.select(statsForUserIdForHotOSMProjectSQL)
                .bind("userId", userId)
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


    /**
     * Retrieves the most used Hashtags in the selected Timeperiod.
     *
     * @param hashtag The hashtag to retrieve statistics for.
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
