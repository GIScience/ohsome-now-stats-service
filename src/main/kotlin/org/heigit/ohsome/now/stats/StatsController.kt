package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.models.*
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.buildOhsomeFormat
import org.heigit.ohsome.now.stats.utils.checkIfOnlyOneResult
import org.heigit.ohsome.now.stats.utils.echoRequestParameters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO
import org.springframework.web.bind.annotation.*
import java.time.Instant

import kotlin.system.measureTimeMillis

@Suppress("largeClass")
@CrossOrigin
@RestController
class StatsController {

    @Autowired
    lateinit var repo: StatsRepo

    @Suppress("LongParameterList")
    @Operation(
        summary = "Returns live data from DB",
    )
    @GetMapping("/stats/{hashtags}", produces = ["application/json"])
    fun stats(
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
        endDate: Instant?,

        @Parameter(description = "indicate whether the results should be returned with additional Metadata")
        @RequestParam(name = "ohsomeFormat", defaultValue = "false", required = false)
        ohsomeFormat: Boolean
    ): Map<String, Any> {
        val results = mutableMapOf<String, Map<String, Any>>()
        val executionTime = measureTimeMillis {
            hashtags.forEach { hashtag ->
                results[hashtag] = repo.getStatsForTimeSpan(HashtagHandler(hashtag), startDate, endDate)
            }
        }
        val finalResults = checkIfOnlyOneResult(results)

        return if (!ohsomeFormat) {
            val extraParams = echoRequestParameters(startDate, endDate)
            finalResults + extraParams
        } else {
            buildOhsomeFormat(finalResults, executionTime, httpServletRequest)
        }
    }

    @Operation(summary = "Returns live data from DB aggregated by month")
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
    ): OhsomeFormat<List<StatsResult>> {
        lateinit var response: List<StatsResult>
        val hashtagHandler = HashtagHandler(hashtag)
        val executionTime = measureTimeMillis {
            response =
                buildIntervalStatsResult(repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, interval))
        }
        return build_ohsome_format(response, executionTime, httpServletRequest)
    }


    @Operation(
        summary = "Returns live data from DB aggregated by country",
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


    @Operation(summary = "Returns the most used Hashtag by user count in a given Timeperiod.")
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


    @Operation(summary = "Returns maximum and minimum timestamps of the database.")
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
