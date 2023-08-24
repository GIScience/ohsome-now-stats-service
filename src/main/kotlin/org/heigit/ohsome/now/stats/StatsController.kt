package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.models.*
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.validateIntervalString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

import kotlin.system.measureTimeMillis
import kotlin.time.Duration

@Suppress("largeClass")
@CrossOrigin
@RestController
class StatsController {

    @Autowired
    lateinit var repo: StatsRepo


    @Operation(
        summary = "Returns live summary statistics for one hashtag",
    )
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
    ): OhsomeFormat<StatsResult> {
        val result: StatsResult
        val executionTime = measureTimeMillis {
            result = buildStatsResult(repo.getStatsForTimeSpan(HashtagHandler(hashtag), startDate, endDate))
        }
        return build_ohsome_format(result, executionTime, httpServletRequest)
    }

    @Suppress("LongMethod")
    @Operation(
        summary = "Returns live summary statistics for multiple hashtags. Wildcard-hashtags are disaggregated.",
    )
    @GetMapping("/stats/hashtags/{hashtags}", produces = ["application/json"])
    fun statsHashtags(
        httpServletRequest: HttpServletRequest,
        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        hashtags: Array<String>,

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("startdate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("enddate", required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        endDate: Instant?
    ): OhsomeFormat<Map<String, StatsResult>> {
        val results = mutableMapOf<String, StatsResult>()
        val executionTime = measureTimeMillis {
            for (hashtag in hashtags) {
                results.putAll(
                    buildMultipleStatsResult(
                        repo.getStatsForTimeSpanAggregate(
                            HashtagHandler(hashtag),
                            startDate,
                            endDate
                        )
                    )
                )
            }
        }
        return build_ohsome_format(results, executionTime, httpServletRequest)
    }

    @Operation(summary = "Returns live summary statistics for one hashtag grouped by a given time interval")
    @GetMapping("/stats/{hashtag}/interval", produces = ["application/json"])
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
    ): OhsomeFormat<List<StatsIntervalResult>> {
        validateIntervalString(interval)
        lateinit var response: List<StatsIntervalResult>
        val hashtagHandler = HashtagHandler(hashtag)
        val executionTime = measureTimeMillis {
            response =
                buildIntervalStatsResult(repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, interval))
        }
        return build_ohsome_format(response, executionTime, httpServletRequest)
    }


    @Operation(
        summary = "Returns live summary statistics for one hashtag grouped by country"
    )
    @GetMapping("/stats/{hashtag}/country", produces = ["application/json"])
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
    ): OhsomeFormat<List<CountryStatsResult>> {
        lateinit var response: List<CountryStatsResult>
        val hashtagHandler = HashtagHandler(hashtag)
        val executionTime = measureTimeMillis {
            response = buildCountryStatsResult(repo.getStatsForTimeSpanCountry(hashtagHandler, startDate, endDate))
        }
        return build_ohsome_format(response, executionTime, httpServletRequest)
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
        lateinit var response: List<HashtagResult>
        val executionTime = measureTimeMillis {
            response = buildHashtagResult(
                repo.getMostUsedHashtags(
                    startDate,
                    endDate,
                    limit,
                )
            )
        }
        return build_ohsome_format(response, executionTime, httpServletRequest)
    }


    @Operation(summary = "Returns maximum and minimum timestamps of the database")
    @GetMapping("/metadata", produces = ["application/json"])
    fun metadata(
        httpServletRequest: HttpServletRequest
    ): OhsomeFormat<MetadataResult> {
        lateinit var response: MetadataResult
        val executionTime = measureTimeMillis {
            val queryResult = repo.getMetadata()
            response = buildMetadataResult(queryResult)
        }
        return build_ohsome_format(response, executionTime, httpServletRequest)
    }
}
