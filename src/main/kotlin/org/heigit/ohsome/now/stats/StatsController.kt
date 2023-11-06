package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.models.*
import org.heigit.ohsome.now.stats.utils.validateIntervalString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.*
import java.time.Instant

import kotlin.system.measureTimeMillis


class Measured<T>(val payload: T, val executionTime: Long)

fun <T> measure(command: () -> T): Measured<T> {
    val result: T
    val executionTime = measureTimeMillis {
        result = command.invoke()
    }

    return Measured(result, executionTime)
}


@CrossOrigin
@RestController
class StatsController {

    @Autowired
    lateinit var statsService: StatsService


    @Suppress("LongParameterList")
    @Operation(summary = "Returns live summary statistics for one hashtag")
    @GetMapping("/stats/{hashtag}", produces = ["application/json"])
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

        @Parameter(description = "A comma separated list of countries, can also only be one country")
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>?,
    ): OhsomeFormat<StatsResult> {

        val result = measure {
            getStatsForTimeSpan(hashtag, startDate, endDate, countries!!)
        }

        return buildOhsomeFormat(result.payload, result.executionTime, httpServletRequest)
    }


    @Suppress("LongMethod")
    @Operation(summary = "Returns live summary statistics for multiple hashtags. Wildcard-hashtags are disaggregated.")
    @GetMapping("/stats/hashtags/{hashtags}", produces = ["application/json"])
    fun statsHashtags(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        hashtags: List<String>,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?
    ): OhsomeFormat<Map<String, StatsResult>> {

        val results = measure {
            getStatsForTimeSpanAggregate(hashtags, startDate, endDate)
        }

        return buildOhsomeFormat(results.payload, results.executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by a given time interval")
    @GetMapping("/stats/{hashtag}/interval", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun statsInterval(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        hashtag: String,

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
        interval: String,

        @Parameter(description = "A comma separated list of countries, can also only be one country")
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>?
    ): OhsomeFormat<List<StatsIntervalResult>> {

        validateIntervalString(interval)

        val response = measure {
            getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries!!)
        }

        return buildOhsomeFormat(response.payload, response.executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by country")
    @GetMapping("/stats/{hashtag}/country", produces = ["application/json"])
    fun statsCountry(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        hashtag: String,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?
    ): OhsomeFormat<List<CountryStatsResult>> {

        val response = measure {
            getStatsForTimeSpanCountry(hashtag, startDate, endDate)
        }

        return buildOhsomeFormat(response.payload, response.executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns the most used Hashtag by user count in a given timeperiod")
    @GetMapping("/most-used-hashtags", produces = ["application/json"])
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
    ): OhsomeFormat<List<HashtagResult>> {

        val response = measure {
            getMostUsedHashtags(startDate, endDate, limit)
        }

        return buildOhsomeFormat(response.payload, response.executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns maximum and minimum timestamps of the database")
    @GetMapping("/metadata", produces = ["application/json"])
    fun metadata(httpServletRequest: HttpServletRequest): OhsomeFormat<MetadataResult> {

        val response = measure {
            getMetadata()
        }

        return buildOhsomeFormat(response.payload, response.executionTime, httpServletRequest)
    }


    private fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?, countries: List<String>) =
        this.statsService.getStatsForTimeSpan(hashtag, startDate, endDate, countries)


    private fun getStatsForTimeSpanAggregate(hashtags: List<String>, startDate: Instant?, endDate: Instant?) =
        this.statsService.getStatsForTimeSpanAggregate(hashtags, startDate, endDate)


    @Suppress("LongParameterList")
    private fun getStatsForTimeSpanInterval(hashtag: String, startDate: Instant?, endDate: Instant?, interval: String, countries: List<String>) =
        this.statsService.getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries)


    private fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?) =
        this.statsService.getStatsForTimeSpanCountry(hashtag, startDate, endDate)


    private fun getMostUsedHashtags(startDate: Instant?, endDate: Instant?, limit: Int?) =
        this.statsService.getMostUsedHashtags(startDate, endDate, limit)


    private fun getMetadata() = this.statsService.getMetadata()



}
