package org.heigit.ohsome.stats

import org.heigit.ohsome.stats.utils.HashtagHandler
import org.heigit.ohsome.stats.utils.getGroupbyInterval
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
class StatsRepo {
    //please add valuable docs here

    @Autowired
    lateinit var dataSource: DataSource

    private val logger: Logger = LoggerFactory.getLogger(StatsRepo::class.java)

    //language=sql
    private fun getStatsFromTimeSpan(hashtagHandler: HashtagHandler, noCache: Boolean) = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length_delta)/1000 as roads,
            sum(ifNull(building_edit,0)) as buildings,
            count(*) as edits,
            max(changeset_timestamp) as latest
        FROM "stats"
        WHERE
            ${if (hashtagHandler.isWildCard) "startsWith" else "equals"}(hashtag, ?) 
            and changeset_timestamp > parseDateTimeBestEffortOrNull(?) 
            and changeset_timestamp < parseDateTimeBestEffortOrNull(?);
            ${if (noCache == true) "settings min_bytes_to_use_direct_io=1" else ""}

        """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    private fun getStatsFromTimeSpanInterval(hashtagHandler: HashtagHandler, noCache: Boolean) = """
        SELECT 
            count(distinct user_id) as users,
            sum(road_length_delta)/1000 as roads,
            sum(ifNull(building_edit,0)) as buildings,
            count(*) as edits,
            toStartOfInterval(changeset_timestamp, INTERVAL ?)::DateTime as startdate,
            toStartOfInterval(changeset_timestamp, INTERVAL ?)::DateTime + INTERVAL ? as enddate
        FROM "stats"
        WHERE
            ${if (hashtagHandler.isWildCard) "startsWith" else "equals"}(hashtag, ?)  
            AND changeset_timestamp > ? 
            AND changeset_timestamp < ?
        GROUP BY 
        startdate 
        ${if (noCache == true) "settings min_bytes_to_use_direct_io=1" else ""}
        """.trimIndent()


    @Suppress("LongMethod")
    //language=sql
    private fun getStatsFromTimeSpanCountry(hashtagHandler: HashtagHandler, noCache: Boolean) = """
        SELECT
            count(distinct user_id) as users,
            sum(road_length_delta)/1000 as roads,
            sum(ifNull(building_edit,0) ) as buildings,
            count(*) as edits,
            max(changeset_timestamp) as latest,
            country_iso_a3 as country
        FROM "stats"
        ARRAY JOIN country_iso_a3
        WHERE
            ${if (hashtagHandler.isWildCard) "startsWith" else "equals"}(hashtag, ?)
            and changeset_timestamp > parseDateTimeBestEffortOrNull(?) 
            and changeset_timestamp < parseDateTimeBestEffortOrNull(?)
        GROUP BY
            country
        ${if (noCache == true) "settings min_bytes_to_use_direct_io=1" else ""}
        """.trimIndent()

    //language=sql
    private fun statsForUserIdForHotOSMProject(noCache: Boolean) = """
    SELECT
        sum(ifNull(building_edit,0)) as building_count,
        sum(road_length_delta)/1000 as road_length,
        count(*) as object_edits,
        user_id
    FROM stats
    WHERE
        user_id = ?
        AND startsWith(hashtag, '#hotosm-project-')
    GROUP BY user_id
    ${if (noCache == true) "settings min_bytes_to_use_direct_io=1" else ""}
""".trimIndent()

    //language=sql
    private fun mostUsedHashtags(noCache: Boolean) = """
        SELECT 
            hashtag, COUNT(DISTINCT user_id) as number_of_users
        FROM stats
        WHERE
            changeset_timestamp > ? and changeset_timestamp < ?
        GROUP BY hashtag
        ORDER BY number_of_users DESC
        LIMIT ?
        ${if (noCache == true) "settings min_bytes_to_use_direct_io=1" else ""}
    """.trimIndent()

    //language=sql
    private fun metadata(noCache: Boolean) = """
        SELECT 
            max(changeset_timestamp) as max_timestamp,
            min(changeset_timestamp) as min_timestamp
        FROM stats
        WHERE changeset_timestamp > now() - toIntervalMonth(1) 
        OR changeset_timestamp < parseDateTimeBestEffortOrNull('2009-04-23T00:00:00.000000Z')
        ${if (noCache == true) "settings min_bytes_to_direct_io=1" else ""}

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
        noCache: Boolean = false
    ): Map<String, Any> {
        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate")

        return create(dataSource).withHandle<Map<String, Any>, RuntimeException> {
            it.select(
                getStatsFromTimeSpan(hashtagHandler, noCache),
                "#${hashtagHandler.hashtag}",
                startDate ?: EPOCH,
                endDate ?: now()
            ).mapToMap().single()
        } + ("hashtag" to hashtagHandler.hashtag)
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
        startDate: Instant? = EPOCH,
        endDate: Instant? = now(),
        interval: String,
        noCache: Boolean = false
    ): List<Map<String, Any>> {
        logger.info("Getting stats for hashtag: ${hashtagHandler.hashtag}, startDate: $startDate, endDate: $endDate, interval: $interval")

        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            it.select(
                getStatsFromTimeSpanInterval(hashtagHandler, noCache),
                getGroupbyInterval(interval),
                getGroupbyInterval(interval),
                getGroupbyInterval(interval),
                "#${hashtagHandler.hashtag}",
                startDate,
                endDate
            ).mapToMap().list()
        }
    }

    /**
     * Retrieves aggregated HOT-TM-project statistics for a specific user.
     * ATTENTION: EXPOSING THIS QUERY MIGHT VIOLATE DATA PRIVACY
     * @param user_id the osm userid which should be queried.
     * @return A list of maps containing the statistics for all hotTM projects.
     */
    fun getStatsForUserIdForAllHotTMProjects(
        user_id: String,
        noCache: Boolean = false
    ): MutableMap<String, Any> {
        logger.info("Getting HotOSM stats for user: ${user_id}")

        return create(dataSource).withHandle<MutableMap<String, Any>, RuntimeException> {
            it.select(
                statsForUserIdForHotOSMProject(noCache),
                user_id
            ).mapToMap().single()
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
        endDate: Instant? = now(),
        noCache: Boolean = false
    ): List<Map<String, Any>> {
        val result = create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            it.select(
                getStatsFromTimeSpanCountry(hashtagHandler, noCache),
                "#${hashtagHandler.hashtag}",
                startDate,
                endDate
            ).mapToMap().list()
        }
        return result
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
        endDate: Instant? = now(),
        limit: Int? = 10,
        noCache: Boolean = false
    ): List<Map<String, Any>> {
        logger.info("Getting trending hashtags startDate: $startDate, endDate: $endDate, limit: $limit")
        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            it.select(
                mostUsedHashtags(noCache),
                startDate,
                endDate,
                limit
            ).mapToMap().list()
        }
    }

    /**
     * Get min_timestamp and max_timestamp for the entire database.
     *
     * @return  A map containing the two keys.
     */
    fun getMetadata(
        noCache: Boolean = false
    ): Map<String, Any> {
        return create(dataSource).withHandle<Map<String, Any>, RuntimeException> {
            it.select(
                metadata(noCache)
            ).mapToMap().single()
        }
    }


}
