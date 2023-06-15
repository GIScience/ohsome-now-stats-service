package org.heigit.ohsome.stats

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi.create
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Instant.EPOCH
import java.time.Instant.now
import java.time.Duration
import javax.sql.DataSource

@Component
class StatsRepo {


    @Autowired
    lateinit var dataSource: DataSource


    //language=SQL
    val stats = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length) as roads,
            count(building_area) as buildings,
            count(*) as edits,s
            FROM_UNIXTIME(intDiv(max(changeset_timestamp), 1000)) as latest
        FROM "stats"
        WHERE
            hashtag = ?;
        """.trimIndent()

    val statsFromTimeSpan = """
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

    val statsFromTimespanInterval = """
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
            startdate
    """.trimIndent()

    fun getStats(hashtag: String) = create(dataSource).withHandle<Map<String, Any>, RuntimeException> { asMap(it, "#$hashtag") } + ("hashtag" to hashtag)


    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?) = create(dataSource)
            .withHandle<Map<String, Any>, RuntimeException> {
                asMapFromTimeSpan(it, "#$hashtag", startDate ?: EPOCH, endDate ?: now())
            } + ("hashtag" to hashtag)

    fun getStatsForTimeSpanInterval(hashtag: String, startDate: Instant, endDate: Instant, interval: String) = create(dataSource).withHandle<List<Map<String, Any>>, RuntimeException> {
            asMapFromTimeSpanInterval(it, "#$hashtag", startDate, endDate,  getGroupbyInterval(interval))
        }

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
     * @return A tuple containing the aggregator and grouper strings for the ClickHouse query.
     *         If the interval is not in the expected format, the aggregator and grouper strings are empty.
     *         ? is this how we want it to be?
     */
    fun getGroupbyInterval(interval: String): String {
        // Regular expression pattern to match the ISO 8601 interval format
        val pattern = """^P(\d+Y)?(\d+M)?(\d+W)?(\d+D)?(T(\d+H)?(\d+M)?)?$""".toRegex()
        val matchResult = pattern.find(interval)

        if (matchResult == null) { // maybe throw error instead
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
    private fun asMapFromTimeSpan(handle: Handle, hashtag: String, startDate: Instant, endDate: Instant) = handle.select(statsFromTimeSpan, hashtag, startDate.toEpochMilli(), endDate.toEpochMilli()).mapToMap().single()
    private fun asMapFromTimeSpanInterval(handle: Handle, hashtag: String, startDate: Instant, endDate: Instant, groupBy: String): List<Map<String, Any>> = handle.select(statsFromTimespanInterval, groupBy , groupBy , groupBy , hashtag, startDate.toEpochMilli(), endDate.toEpochMilli()).mapToMap().list()
}
