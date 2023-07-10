package org.heigit.ohsome.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.math.NumberUtils.toDouble
import org.heigit.ohsome.stats.utils.HashtagHandler
import org.heigit.ohsome.stats.utils.buildOhsomeFormat
import org.heigit.ohsome.stats.utils.echoRequestParameters
import org.heigit.ohsome.stats.utils.makeUrl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.HandlerMapping
import java.time.Instant
import java.time.Instant.EPOCH
import java.time.Instant.now
import kotlin.random.Random

import kotlin.system.measureTimeMillis

@Suppress("largeClass")
@CrossOrigin
@RestController
class StatsController {

    @Autowired
    lateinit var repo: StatsRepo


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

    @Suppress("LongParameterList")
    @Operation(summary = "Returns live data from DB")
    @GetMapping("/stats/{hashtag}")
    fun stats(
        httpServletRequest: HttpServletRequest,
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
        endDate: Instant?,

        @Parameter(description = "indicate whether the results should be returned with additional Metadata")
        @RequestParam(name = "ohsomeFormat", defaultValue = "false", required = false)
        ohsomeFormat: Boolean
    ): Map<String, Any> {
        val hashtagHandler = HashtagHandler(hashtag)
        lateinit var stats: Map<String, Any>
        val executionTime = measureTimeMillis {
            stats = repo.getStatsForTimeSpan(hashtagHandler, startDate, endDate)
        }
        return if (!ohsomeFormat) {
            val extraParams = echoRequestParameters(startDate, endDate)
            stats + extraParams
        } else {
            buildOhsomeFormat(stats, executionTime, httpServletRequest)
        }
    }

    @Operation(summary = "Returns live data from DB aggregated by month")
    @GetMapping("/stats/{hashtag}/interval")
    @Suppress("LongParameterList")
    fun statsInterval(
        httpServletRequest: HttpServletRequest,
        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable hashtag: String,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?,

        @Parameter(description = "the granularity defined as Intervals in ISO 8601 time format eg: P1M")
        @RequestParam(name = "interval", defaultValue = "P1M", required = false)
        interval: String
    ): Map<String, Any> {
        lateinit var response: List<Any>
        val hashtagHandler = HashtagHandler(hashtag)
        val executionTime = measureTimeMillis {
            response = repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, interval)
        }
        return buildOhsomeFormat(response, executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns live data from DB aggregated by country")
    @GetMapping("/stats/{hashtag}/country")
    fun statsCountry(
        httpServletRequest: HttpServletRequest,
        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable hashtag: String,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?
    ): Map<String, Any> {
        lateinit var response: List<Map<String, Any>>
        val hashtagHandler = HashtagHandler(hashtag)
        val executionTime = measureTimeMillis {
            response = repo.getStatsForTimeSpanCountry(hashtagHandler, startDate, endDate)
        }
        return buildOhsomeFormat(response, executionTime, httpServletRequest)
    }

    @Operation(summary = "Returns aggregated HOT-TM-project statistics for a specific user.")
    @GetMapping("/stats/HotTMUser")
    fun statsHotTMUserStats(
        httpServletRequest: HttpServletRequest,
        @Parameter(description = "OSM user id")
        @RequestParam(name = "userId")
        userId: String
    ): Map<String, Any> {
        lateinit var response: MutableMap<String, Any>
        val executionTime = measureTimeMillis {
            response = repo.getStatsForUserIdForAllHotTMProjects(userId)
        }
        response["building_count"] = Random.nextInt(1, 100)
        response["road_length"] = Random.nextDouble(1.0, 1000.0)
        response["object_edits"] = Random.nextInt(100, 2000)
        
        return response
    }

    @Operation(summary = "Returns the most used Hashtag by user count in a given Timeperiod.")
    @GetMapping("/mostUsedHashtags")
    fun mostUsedHashtags(
        httpServletRequest: HttpServletRequest,
        @Parameter(description = "the start date for the query in ISO format (e.g. 2014-01-01T00:00:00Z). Default: start of data")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2023-01-01T00:00:00Z). Default: now")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?,

        @Parameter(description = "the number of hashtags to return")
        @RequestParam(name = "limit", required = false, defaultValue = "10")
        limit: Int?
    ): Map<String, Any> {
        lateinit var response: List<Any>
        val executionTime = measureTimeMillis {
            response = repo.getMostUsedHashtags(
                startDate,
                endDate,
                limit,
            )
        }
        return buildOhsomeFormat(response, executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns maximum and minimum timestamps of the database.")
    @GetMapping("/metadata")
    fun metadata(
        httpServletRequest: HttpServletRequest
    ): Map<String, Any> {
        lateinit var response: Map<String, Any>
        val executionTime = measureTimeMillis {
            val queryResult = repo.getMetadata()
            response = queryResult
        }
        return buildOhsomeFormat(response, executionTime, httpServletRequest)
    }
}
