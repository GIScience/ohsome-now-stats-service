package org.heigit.ohsome.now.statsservice.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.*
import org.heigit.ohsome.now.statsservice.topic.ValidTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant


// TODO: consider using this class instead of simple string to prevent primitive obsession
// @Schema(type = "string")
// class Hashtag(@get:ValidHashtag val value: String)

@Suppress("LargeClass")
@CrossOrigin
@RestController
@Validated
class StatsController {

    @Autowired
    lateinit var statsService: StatsService


    @Suppress("LongParameterList")
    @Operation(summary = "Returns live summary statistics for all contributions optionally filtered by one hashtag")
    @GetMapping("/stats", produces = ["application/json"])
    fun stats(
        httpServletRequest: HttpServletRequest,

        @HashtagConfig
        @RequestParam("hashtag", required = false, defaultValue = "")
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam("startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam("enddate", required = false)
        endDate: Instant?,

        @CountriesConfig
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>,

        @TopicsConfig
        @RequestParam("topics", required = true)
        topics: List<@ValidTopic String>
    ): OhsomeFormat<StatsResultWithTopics> {
        val result = measure {
            statsService.getStatsForTimeSpan(hashtag, startDate, endDate, countries, topics)
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

    @Operation(summary = "Returns live summary statistics for all contributions, optionally filtered by one hashtag, grouped by a given time interval")
    @GetMapping("/stats/interval", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun statsInterval(
        httpServletRequest: HttpServletRequest,

        @HashtagConfig
        @RequestParam("hashtag", required = false, defaultValue = "")
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
        @ParseableInterval
        @AtLeastOneMinuteInterval
        interval: String,

        @CountriesConfig
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>,

        @TopicsConfig
        @RequestParam("topics", required = true)
        topics: List<@ValidTopic String>
    ): OhsomeFormat<StatsIntervalResultWithTopics> {


        val result = measure {
            statsService.getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for all contributions, optionally filtered by one hashtag, grouped by country")
    @GetMapping("/stats/country", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun statsCountry(
        httpServletRequest: HttpServletRequest,

        @HashtagConfig
        @RequestParam(name = "hashtag", required = false, defaultValue = "")
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @TopicsConfig
        @RequestParam("topics", required = true)
        topics: List<@ValidTopic String>
    ): OhsomeFormat<StatsTopicCountryResult> {

        val result = measure {
            statsService.getStatsForTimeSpanCountry(hashtag, startDate, endDate, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }

    @Suppress("LongParameterList")
    @Operation(summary = "Returns live summary statistics grouped into h3 cells")
    @GetMapping("/stats/h3", produces = ["text/csv", "application/json"])
    fun statsByH3(
        @HashtagConfig
        @RequestParam(name = "hashtag", required = false, defaultValue = "")
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @Parameter(description = "topic - this endpoint can only serve results for one topic at a time")
        @RequestParam("topic", required = true)
        @ValidTopic
        topic: String,

        @Parameter(description = "h3 resolution")
        @RequestParam(value = "resolution", required = false, defaultValue = "3")
        @ValidResolution
        resolution: Int,

        @CountriesConfig
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>
    ): String {
        return statsService.getStatsByH3(hashtag, startDate, endDate, topic, resolution, countries)
    }

    @Suppress("LongParameterList")
    @Operation(summary = "Returns the most used Hashtag by user count in a given timeperiod")
    @GetMapping("/most-used-hashtags", produces = ["application/json"])
    fun mostUsedHashtags(
        httpServletRequest: HttpServletRequest,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @Parameter(description = "the number of hashtags to return")
        @RequestParam(name = "limit", required = false, defaultValue = "10")
        limit: Int?,

        @CountriesConfig
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>?
    ): OhsomeFormat<List<HashtagResult>> {

        val result = measure {
            statsService.getMostUsedHashtags(startDate, endDate, limit, countries!!)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns all hashtags with at least 10 contributions contained in the database")
    @GetMapping("/hashtags", produces = ["application/json"])
    fun hashtags(
        httpServletRequest: HttpServletRequest
    ): OhsomeFormat<List<UniqueHashtagsResult>> {

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
