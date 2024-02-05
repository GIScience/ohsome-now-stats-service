package org.heigit.ohsome.now.statsservice.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.*
import org.heigit.ohsome.now.statsservice.utils.validateIntervalString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant


// TODO: consider using this class instead of simple string to prevent primitive obsession
// @Schema(type = "string")
// class Hashtag(@get:ValidHashtag val value: String)

@CrossOrigin
@RestController
@Validated
class StatsController {

    @Autowired
    lateinit var statsService: StatsService


    @Suppress("LongParameterList")
    @Operation(summary = "Returns live summary statistics for one hashtag")
    @GetMapping("/stats/{hashtag}", produces = ["application/json"])
    fun stats(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'" )
        @PathVariable
//        @Valid
//        hashtag: Hashtag,
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam("startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam("enddate", required = false)
        endDate: Instant?,

        @Parameter(description = "A comma separated list of countries, can also only be one country")
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>?,
    ): OhsomeFormat<StatsResult> {

        val result = measure {
            statsService.getStatsForTimeSpan(hashtag, startDate, endDate, countries!!)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Suppress("LongMethod")
    @Operation(summary = "Returns live summary statistics for multiple hashtags. Wildcard-hashtags are disaggregated.")
    @GetMapping("/stats/hashtags/{hashtags}", produces = ["application/json"])
    fun statsHashtags(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtags to query for - case-insensitive and without the leading '#'")
        @PathVariable
        hashtags: List<@ValidHashtag String>,
//        hashtags: List<@Valid Hashtag>,

        @StartDateConfig
        @RequestParam("startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam("enddate", required = false)
        endDate: Instant?
    ): OhsomeFormat<Map<String, StatsResult>> {

        val result = measure {
            statsService.getStatsForTimeSpanAggregate(hashtags, startDate, endDate)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by a given time interval")
    @GetMapping("/stats/{hashtag}/interval", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun statsInterval(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @Parameter(description = "the granularity defined as Intervals in ISO 8601 time format eg: P1M")
        @RequestParam(name = "interval", defaultValue = "P1M", required = false)
        interval: String,

        @Parameter(description = "A comma separated list of countries, can also only be one country")
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>?
    ): OhsomeFormat<StatsIntervalResult> {

        validateIntervalString(interval)

        val result = measure {
            statsService.getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries!!)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by country")
    @GetMapping("/stats/{hashtag}/country", produces = ["application/json"])
    fun statsCountry(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @PathVariable
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?
    ): OhsomeFormat<List<CountryStatsResult>> {

        val result = measure {
            statsService.getStatsForTimeSpanCountry(hashtag, startDate, endDate)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns the most used Hashtag by user count in a given timeperiod")
    @GetMapping("/most-used-hashtags", produces = ["application/json"])
    fun mostUsedHashtags(
        httpServletRequest: HttpServletRequest,

        //TODO: check if this description really should be different from the one above
        @Parameter(description = "the start date for the query in ISO format (e.g. 2014-01-01T00:00:00Z). Default: start of data")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = DATE_TIME)
        startDate: Instant?,

        //TODO: check if this description really should be different from the one above
        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2023-01-01T00:00:00Z). Default: now")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = DATE_TIME)
        endDate: Instant?,

        @Parameter(description = "the number of hashtags to return")
        @RequestParam(name = "limit", required = false, defaultValue = "10")
        limit: Int?
    ): OhsomeFormat<List<HashtagResult>> {

        val result = measure {
            statsService.getMostUsedHashtags(startDate, endDate, limit)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns all hashtags with at least 10 contributions contained in the database")
    @GetMapping("/hashtags", produces = ["application/json"])
    fun hashtags(httpServletRequest: HttpServletRequest): OhsomeFormat<List<UniqueHashtagsResult>> {

        val result = measure {
            statsService.getUniqueHashtags()
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns maximum and minimum timestamps of the database")
    @GetMapping("/metadata", produces = ["application/json"])
    fun metadata(httpServletRequest: HttpServletRequest): OhsomeFormat<MetadataResult> {

        val result = measure {
            statsService.getMetadata()
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


}
