package org.heigit.ohsome.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.*
import java.time.Instant
import kotlin.system.measureTimeMillis


@CrossOrigin
@RestController
class StatsController {

    private val logger = LoggerFactory.getLogger(StatsController::class.java)

    @Autowired
    lateinit var repo: StatsRepo


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
        return this.repo.getStatsForTimeSpan(hashtag, startDate, endDate) +
                echoRequestParameters(startDate, endDate)

    }


    fun echoRequestParameters(startDate: Instant?, endDate: Instant?): Map<String, Instant> {
        var extraMap = emptyMap<String, Instant>()

        startDate?.let {
            extraMap = extraMap + mapOf("startdate" to startDate)
        }

        endDate?.let {
            extraMap = extraMap + mapOf("enddate" to endDate)
        }

        return extraMap
    }

    // static data taken from http://osm-stats-production-api.azurewebsites.net/stats at 2pm, 20 March 2023
    @Operation(summary = "Returns a static snapshot of OSM statistics (for now)")
    @GetMapping("/stats_static")
    fun statsStatic() = mapOf(
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
     * @param startDate the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z) (optional)
     * @param endDate the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z) (optional)
     * @param interval the granularity defined as Intervals in ISO 8601 time format (e.g. P1M) (optional, defaults to auto)
     * @return a map containing the response data with aggregated statistics
     */

    fun listMapToString(list: List<Map<String, Any>>): String {
        val stringBuilder = StringBuilder()

        for (map in list) {
            for ((key, value) in map) {
                stringBuilder.append("$key: $value\n")
            }
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }
    @Operation(summary = "Returns live data from DB aggregated by month")
    @GetMapping("/stats/{hashtag}/interval")
    fun statsInterval(
            @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
            @PathVariable
            hashtag: String,

            @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
            @RequestParam("startdate", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME)
            startDate: Instant = Instant.ofEpochSecond(0),

            @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
            @RequestParam("enddate", required = false)
            @DateTimeFormat(iso = ISO.DATE_TIME)
            endDate: Instant = Instant.now(),

            @Parameter(description = "the granularity defined as Intervals in ISO 8601 time format eg: P1M")
            @RequestParam("interval", required = false,) //defaults to auto
            interval: String = "auto"

            /*Alternativly
            @Parameter(description = "the timerange defined start date and enddate for the query in ISO  8601 format (e.g. 2020-01-01T00:00:00Z) and the interval also in the ISO format 8601 ")
            @RequestParam("timerange", required = true) //is it possible to set the independent parts to diffent defaults?
            timetrange: String

            //this function is used to split the iso 8601 String into startdate, enddate and interval
            fun splitISOInterval(isoInterval: String): Triple<Instant, Instant, String> {
                val parts = isoInterval.split("/")
                if (parts.size != 3) {
                    throw IllegalArgumentException("Invalid ISO 8601 interval format.")
                }

                val start = Instant.parse(parts[0])
                val end = Instant.parse(parts[1])
                val duration = parts[2]

                return Triple(start, end, duration)
            }
             */
    ): Map<String, Any> {
        val response = mutableMapOf<String, Any>()

        val executionTime = measureTimeMillis {
            val queryResult = this.repo.getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval)
            logger.info(listMapToString(queryResult))
            response["result"] = queryResult
        }

        response["attribution"] = mapOf("url" to "https://ohsome.org/copyrights", "text" to "© OpenStreetMap contributors")
        response["apiVersion"] = "1.9.0"
        response["metadata"] = mapOf(
                "executionTime" to executionTime,
                "requestUrl" to "https://stats.ohsome.org/..." //ToDo
        )
        response["query"] = mapOf(
                "timespan" to mapOf(
                        "startDate" to (startDate).toString(),
                        "endDate" to (endDate).toString(),
                        "interval" to interval
                ),
                "hashtag" to hashtag

        )
        response["latest"] = Instant.now().toString() //ToDo

        return response
    }

}
