package org.heigit.ohsome.stats

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi.create
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Instant.EPOCH
import java.time.Instant.now
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class StatsRepo {

    @Autowired
    lateinit var dataSource: DataSource

    private val logger: Logger = LoggerFactory.getLogger(StatsRepo::class.java)

    //language=SQL
    private val stats = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length) as roads,
            count(building_area) as buildings,
            count(*) as edits,
            FROM_UNIXTIME(intDiv(max(changeset_timestamp), 1000)) as latest
        FROM "stats"
        WHERE
            hashtag = ?;
        """.trimIndent()

    private val statsFromTimeSpan = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length) as roads,
            count(building_area) as buildings,
            count(*) as edits,
            FROM_UNIXTIME(intDiv(max(changeset_timestamp), 1000)) as latest
        FROM "stats"
        WHERE
            hashtag = ? and changeset_timestamp > ? and changeset_timestamp < ?;
        """.trimIndent()

    private val statsFromTimeSpanInterval = """
        SELECT 
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length) as roads,
            count(building_area) as buildings,
            count(*) as edits,
            toStartOfInterval(fromUnixTimestamp((changeset_timestamp / 1000)::integer), INTERVAL ?) as startdate,
            toStartOfInterval(fromUnixTimestamp((changeset_timestamp / 1000)::integer), INTERVAL ?) + INTERVAL ? as enddate
        FROM "stats"
        WHERE
            hashtag = ? and changeset_timestamp > ? and changeset_timestamp < ?
        GROUP BY 
            startdate;
    """.trimIndent()

    /**
     * Retrieves statistics for a specific hashtag.
     *
     * @param hashtag The hashtag to retrieve statistics for.
     * @return A map containing the statistics.
     */
    fun getStats(hashtag: String): Map<String, Any> {
        logger.info("Getting stats for hashtag: $hashtag")
        return create(dataSource).withHandle<Map<String, Any>, RuntimeException> { asMap(it, "#$hashtag") } + ("hashtag" to hashtag)
    }

    /**
     * Retrieves statistics for a specific hashtag within a time span.
     *
     * @param hashtag The hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @return A map containing the statistics.
     */
    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?): Map<String, Any> {
        logger.info("Getting stats for hashtag: $hashtag, startDate: $startDate, endDate: $endDate")
        return create(dataSource).withHandle<Map<String, Any>, RuntimeException> {
            asMapFromTimeSpan(it, "#$hashtag", startDate ?: EPOCH, endDate ?: now())
        } + ("hashtag" to hashtag)
    }

    /**
     * Retrieves statistics for a specific hashtag within a time span and interval.
     *
     * @param hashtag The hashtag to retrieve statistics for.
     * @param startDate The start date of the time span.
     * @param endDate The end date of the time span.
     * @param interval The interval for grouping the statistics.
     * @return A list of maps containing the statistics for each interval.
     */
    fun getStatsForTimeSpanInterval(hashtag: String, startDate: Instant, endDate: Instant, interval: String): List<Map<String, Any>> {
        logger.info("Getting stats for hashtag: $hashtag, startDate: $startDate, endDate: $endDate, interval: $interval")
        return create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            asMapFromTimeSpanInterval(it, "#$hashtag", startDate, endDate, getGroupbyInterval(interval))
        }
    }

    /**
     * Converts a duration to its SQL string representation.
     *
     * @return The SQL string representation of the duration.
     */
    fun Duration.toSqlString(): String {
        val days = toDays()
        val hours = toHoursPart()
        val minutes = toMinutesPart()
        return "INTERVAL '${days}D ${hours}H ${minutes}M'"
    }

    /**
     * Translates a custom ISO 8601 interval to the corresponding ClickHouse query for aggregator and grouper.
     *
     * @param interval The custom ISO 8601 interval string.
     * @return The aggregator string for the ClickHouse query.
     *         If the interval is not in the expected format, an empty string is returned.
     */
    fun getGroupbyInterval(interval: String): String {
        // Regular expression pattern to match the ISO 8601 interval format
        val pattern = """^P(\d+Y)?(\d+M)?(\d+W)?(\d+D)?(T(\d+H)?(\d+M)?)?$""".toRegex()
        val matchResult = pattern.find(interval)

        if (matchResult == null) {
            logger.error("Invalid interval format: $interval")
            return ""
        }

        // Extract the components (years, months, weeks, days, hours, minutes) from the interval
        val (years, months, weeks, days, hours, minutes) = matchResult.destructured

        // Build the aggregator string based on the extracted components
        val aggregator = buildString {
            if (!years.isNullOrEmpty()) {
                append(years.removeSuffix("Y"))
                append(" YEAR")
            } else if (!months.isNullOrEmpty()) {
                append(months.removeSuffix("M"))
                append(" MONTH")
            } else if (!weeks.isNullOrEmpty()) {
                append(weeks.removeSuffix("W"))
                append(" WEEK")
            } else if (!days.isNullOrEmpty()) {
                append(days.removeSuffix("D"))
                append(" DAY")
            } else if (!hours.isNullOrEmpty()) {
                append(hours.removePrefix("T").removeSuffix("H"))
                append(" HOUR")
            } else if (!minutes.isNullOrEmpty()) {
                append(minutes.removePrefix("T").removeSuffix("M"))
                append(" MINUTE")
            }
        }

        return aggregator.trim()
    }

    private fun asMap(handle: Handle, hashtag: String) = handle.select(stats, hashtag).mapToMap().single()
    private fun asMapFromTimeSpan(handle: Handle, hashtag: String, startDate: Instant, endDate: Instant)
            = handle.select(statsFromTimeSpan, hashtag, startDate.toEpochMilli(), endDate.toEpochMilli()).mapToMap().single()
    private fun asMapFromTimeSpanInterval(handle: Handle, hashtag: String, startDate: Instant, endDate: Instant, groupBy: String): List<Map<String, Any>> = handle.select(statsFromTimeSpanInterval, groupBy, groupBy, groupBy, hashtag, startDate.toEpochMilli(), endDate.toEpochMilli()).mapToMap().list()
}
