package org.heigit.ohsome.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Optional
import kotlin.system.measureTimeMillis

@CrossOrigin
@RestController
class StatsController {


    @Autowired
    lateinit var repo: StatsRepo

    /**
     * Returns live data from the database for a specific hashtag and time range.
     *
     * @param hashtag the hashtag to query for - case-insensitive and without the leading '#'
     * @param startDate the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)
     * @param endDate the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)
     * @return a map containing the response data with the aggregated statistics and request parameters
     */
    @Operation(summary = "Returns live data from DB")
    @GetMapping("/stats/{hashtag}")
    fun stats(
        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        hashtag: String,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?
    ): Map<String, Any> {
        val stats = repo.getStatsForTimeSpan(hashtag, startDate, endDate)
        val extraParams = echoRequestParameters(startDate, endDate)
        return stats + extraParams
    }

    /**
     * Echoes the request parameters as a map.
     *
     * @param startDate the (inclusive) start date for the query
     * @param endDate the (exclusive) end date for the query
     * @return a map containing the request parameters
     */
    fun echoRequestParameters(startDate: Instant?, endDate: Instant?): Map<String, Instant> {
        val extraParams = mutableMapOf<String, Instant>()
        startDate?.let { extraParams["startdate"] = it }
        endDate?.let { extraParams["enddate"] = it }
        return extraParams
    }

    /**
     * Returns a static snapshot of OSM statistics.
     *
     * @return a map containing the static OSM statistics
     */
    @Operation(summary = "Returns a static snapshot of OSM statistics (for now)")
    @GetMapping("/stats_static")
    fun statsStatic(): Map<String, Any> = mapOf(
        "changesets" to 65009011,
        "users" to 3003842,
        "roads" to 45964973.0494135,
        "buildings" to 844294167,
        "edits" to 1095091515,
        "latest" to "2023-03-20T10:55:38.000Z",
        "hashtag" to "*"
    )

    /**
     * Retrieves live data from the database aggregated by interval.
     *
     * @param hashtag the hashtag to query for - case-insensitive and without the leading '#'
     * @param startDate the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)
     * @param endDate the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)
     * @param interval the granularity defined as Intervals in ISO 8601 time format (e.g. P1M)
     * @return a map containing the response data with aggregated statistics, metadata, and request information
     */
    @Operation(summary = "Returns live data from DB aggregated by month")
    @GetMapping("/stats/{hashtag}/interval")
    fun statsInterval(
        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable hashtag: String,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", defaultValue = "2004-01-01T00:00:00Z", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate", defaultValue = "2020-01-01T00:00:00Z", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant,

        @Parameter(description = "the granularity defined as Intervals in ISO 8601 time format eg: P1M")
        @RequestParam(name = "interval", defaultValue = "P1M", required = false)
        interval: String
    ): Map<String, Any> = getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval)

    private fun getStatsForTimeSpanInterval(
        hashtag: String,
        startDate: Instant,
        endDate: Instant,
        interval: String
    ): Map<String, Any> {

        val response = mutableMapOf<String, Any>()

        val executionTime = measureTimeMillis {
            val queryResult = repo.getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval)
            response["result"] = queryResult
        }

        response["attribution"] =
            mapOf("url" to "https://ohsome.org/copyrights", "text" to "© OpenStreetMap contributors")
        response["apiVersion"] = "tbd"
        response["metadata"] = buildMetadata(executionTime)
        response["query"] = buildQueryInfoTimespan(startDate, endDate, hashtag, interval)
        response["latest"] = Instant.now().toString() // ToDo: Replace with the actual latest timestamp

        return response
    }

    private fun buildMetadata(executionTime: Long): Map<String, Any> {
        return mapOf(
            "executionTime" to executionTime,
            "requestUrl" to "https://stats.ohsome.org/..." // ToDo: Update with the actual request URL
        )
    }

    private fun buildQueryInfoTimespan(
        startDate: Instant,
        endDate: Instant,
        hashtag: String?,
        interval: String?
    ): Map<String, Any> {
        val timespan = mutableMapOf<String, String>()
        timespan["startDate"] = startDate.toString()
        timespan["endDate"] = endDate.toString()
        if (interval != null) timespan["interval"] = interval
        val queryInfo = mutableMapOf<String, Any>()

        queryInfo["timespan"] = timespan

        if (hashtag != null) queryInfo["hashtag"] = hashtag

        return queryInfo
    }

    @Operation(summary = "Returns the top 10 hashtags for a given hashtag")
    @GetMapping("/trending")
    fun trendingHashtags(
        @Parameter(description = "the start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate")
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?,

        @Parameter(description = "the number of hashtags to return")
        @RequestParam(name = "limit", defaultValue = "10")
        limit: Int
    ): Map<String, Any> {
        val finalEndDate = endDate?: Instant.now()
        val finalStartDate = startDate?: finalEndDate.minus(1,ChronoUnit.DAYS)
        return getTrendingHashtags(finalStartDate,finalEndDate, limit)
    }

    private fun getTrendingHashtags(startDate: Instant, endDate: Instant,limit:Int): Map<String, Any> {

        val response = mutableMapOf<String, Any>()
        val executionTime = measureTimeMillis {
            //is there a better way to handle default values?
            val queryResult = repo.getTrendingHashtags(startDate, endDate,limit)
            response["result"] = queryResult
        }

        response["attribution"] =
            mapOf("url" to "https://ohsome.org/copyrights", "text" to "© OpenStreetMap contributors")
        response["apiVersion"] = "tbd"
        response["metadata"] = buildMetadata(executionTime)
        response["query"] = buildQueryInfoTimespan(startDate, endDate, null, null)
        response["latest"] = Instant.now().toString() // ToDo: Replace with the actual latest timestamp

        return response
    }


}
