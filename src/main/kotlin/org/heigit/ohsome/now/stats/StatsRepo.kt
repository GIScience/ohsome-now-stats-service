package org.heigit.ohsome.now.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.getGroupbyInterval
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


//TODO: replace positional with named parameters in sql queries

@Suppress("LargeClass")
@Component
class StatsRepo {
    //please add valuable docs here

    @Autowired
    lateinit var dataSource: DataSource

    private val logger: Logger = LoggerFactory.getLogger(StatsRepo::class.java)


    fun defaultResultForMissingUser(userId: String): MutableMap<String, Any> = mutableMapOf(
        "user_id" to userId.toInt(),
        "buildings" to 0L,
        "roads" to 0.toDouble(),
        "edits" to UnsignedLong.valueOf(0),
        "changesets" to UnsignedLong.valueOf(0)
    )



    //language=sql
    private fun statsFromTimeSpanSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            ifNull(sum(road_length_delta)/1000, 0) as roads,
            ifNull(sum(building_edit), 0) as buildings,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest
        FROM "stats"
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag) 
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
            ${countryHandler.optionalFilterSQL}
        """.trimIndent()


    //language=sql
    private fun statsFromTimeSpanAggregateSQL(hashtagHandler: HashtagHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            ifNull(sum(road_length_delta)/1000, 0) as roads,
            ifNull(sum(building_edit), 0) as buildings,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest,
            hashtag
        FROM "stats"
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag) 
            and changeset_timestamp > parseDateTimeBestEffort(:startDate) 
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY hashtag
        """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    private fun statsFromTimeSpanIntervalSQL(hashtagHandler: HashtagHandler, countryHandler: CountryHandler) = """
    SELECT
        count(distinct changeset_id) as changesets,
        count(distinct user_id) as users,
        ifNull(sum(road_length_delta)/1000, 0) as roads,
        ifNull(sum(building_edit), 0) as buildings,
        count(map_feature_edit) as edits,
        toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as startdate,
        (toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime + INTERVAL :interval) as enddate
    FROM "stats"
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


    @Suppress("LongMethod")
    //language=sql
    private fun statsFromTimeSpanCountrySQL(hashtagHandler: HashtagHandler) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            ifNull(sum(road_length_delta)/1000, 0) as roads,
            ifNull(sum(building_edit), 0) as buildings,
            count(map_feature_edit) as edits,
            max(changeset_timestamp) as latest,
            country_iso_a3 as country
        FROM "stats"
        ARRAY JOIN country_iso_a3
        WHERE
            ${hashtagHandler.variableFilterSQL}(hashtag, :hashtag)
            and changeset_timestamp > parseDateTimeBestEffort(:startDate)
            and changeset_timestamp < parseDateTimeBestEffort(:endDate)
        GROUP BY
            country
        """.trimIndent()


    //language=sql
    private val statsForUserIdForHotOSMProjectSQL = """
        select
            ifNull(sum(building_edit), 0) as buildings,
            ifNull(sum(road_length_delta) /1000, 0) as roads,
            count(map_feature_edit) as edits,
            count(distinct changeset_id) as changesets,
            user_id
        from stats
        where
            user_id = :userId
            and startsWith(hashtag, 'hotosm-project-')
        group by user_id

    """.trimIndent()


    //language=sql
    private val mostUsedHashtagsSQL = """
        SELECT 
            hashtag, COUNT(DISTINCT user_id) as number_of_users
        FROM "stats"
        WHERE
            changeset_timestamp > parseDateTimeBestEffort(:startDate) and 
            changeset_timestamp < parseDateTimeBestEffort(:endDate)
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
        FROM "stats"
        WHERE changeset_timestamp > now() - toIntervalMonth(1) 
        OR changeset_timestamp < parseDateTimeBestEffort('2009-04-23T00:00:00.000000Z')
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

        return create(dataSource).withHandle<Map<String, Any>, RuntimeException> {
            it.select(statsFromTimeSpanSQL(hashtagHandler, countryHandler))
                .bind("hashtag", hashtagHandler.hashtag)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .mapToMap()
                .single()
        } + ("hashtag" to hashtagHandler.hashtag)
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

        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
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
    ): List<Map<String, Any>> {
        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval")

        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            it.select(statsFromTimeSpanIntervalSQL(hashtagHandler, countryHandler))
                .bind("interval", getGroupbyInterval(interval))
                .bind("startdate", startDate ?: EPOCH)
                .bind("enddate", endDate ?: now())
                .bind("hashtag", hashtagHandler.hashtag)
                .mapToMap()
                .list()
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

        return try {
            create(dataSource).withHandle<MutableMap<String, Any>, RuntimeException> {
                it.select(statsForUserIdForHotOSMProjectSQL)
                    .bind("userId", userId)
                    .mapToMap()
                    .single()
            }
        } catch (e: NoSuchElementException) {
            defaultResultForMissingUser(userId)
        }

    }

    /**
     * Retrieves statistics for a specific hashtag grouped by country.
     *
     * @param hashtag The hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A list of maps containing the statistics for each interval.
     */
    fun getStatsForTimeSpanCountry(
        hashtagHandler: HashtagHandler,
        startDate: Instant? = EPOCH,
        endDate: Instant? = now()
    ): List<Map<String, Any>> {

        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
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
        limit: Int? = 10
    ): List<Map<String, Any>> {
        logger.info("Getting trending hashtags startDate: $startDate, endDate: $endDate, limit: $limit")

        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            it.select(mostUsedHashtagsSQL)
                .bind("startDate", startDate ?: EPOCH)
                .bind("endDate", endDate ?: now())
                .bind("limit", limit)
                .mapToMap()
                .list()
        }
    }

    /**
     * Get min_timestamp and max_timestamp for the entire database.
     *
     * @return  A map containing the two keys.
     */
    fun getMetadata() = create(dataSource).withHandle<Map<String, Any>, RuntimeException> {
        it.select(metadataSQL)
            .mapToMap()
            .single()
    }

}
