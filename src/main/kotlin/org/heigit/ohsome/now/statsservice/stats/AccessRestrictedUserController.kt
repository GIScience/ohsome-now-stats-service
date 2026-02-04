package org.heigit.ohsome.now.statsservice.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.*
import org.heigit.ohsome.now.statsservice.topic.ValidTopic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant


@CrossOrigin
@RestController
class AccessRestrictedUserController {


    @Autowired
    lateinit var statsService: StatsService


    @Autowired
    lateinit var appProperties: AppProperties

    @Suppress("LongParameterList")
    @Operation(summary = "Returns aggregated statistics for a specific user.")
    @GetMapping("/stats/user", produces = ["application/json"])
    fun statsByUserId(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "OSM user id")
        @RequestParam("userId")
        userId: String,

        @Parameter(
            description = "the hashtag to query for - case-insensitive and without the leading '#'",
            example = "hotosm-project-*"
        )
        @RequestParam("hashtag", required = false, defaultValue = "")
        @ValidHashtag
        hashtag: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @CountriesConfig
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?,

        @TopicsConfig
        @RequestParam("topics", required = true)
        topics: List<@ValidTopic String>
    ): OhsomeFormat<UserResult> {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token")
        }

        val result = measure {
            statsService.getStatsForTimeSpan(hashtag, startDate, endDate, countries, topics, userId)
                .toUserResult(userId)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }

    @Suppress("LongParameterList")
    @Operation(summary = "Returns aggregated statistics by interval for a specific user.")
    @GetMapping("/stats/user/interval", produces = ["application/json"])
    fun statsByUserIdInterval(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "OSM user id")
        @RequestParam("userId")
        userId: String,

        @Parameter(
            description = "the hashtag to query for - case-insensitive and without the leading '#'",
            example = "hotosm-project-*"
        )
        @RequestParam("hashtag", required = false, defaultValue = "")
        @ValidHashtag
        hashtag: String,

        @Parameter(description = "the granularity defined as Intervals in ISO 8601 time format eg: P1M")
        @RequestParam(name = "interval", defaultValue = "P1M", required = false)
        @ParseableInterval
        @AtLeastOneMinuteInterval
        interval: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @CountriesConfig
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?,

        @TopicsConfig
        @RequestParam("topics", required = true)
        topics: List<@ValidTopic String>
    ): OhsomeFormat<StatsIntervalResultWithTopics> {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token")
        }

        val result = measure {
            statsService.getStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries, topics, userId)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for all contributions, filtered by userId and optionally one hashtag, grouped by country")
    @GetMapping("/stats/user/country", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun statsCountryByUserId(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "OSM user id")
        @RequestParam("userId")
        userId: String,

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
        topics: List<@ValidTopic String>,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?,

        ): OhsomeFormat<StatsTopicCountryResult> {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token")
        }

        val result = measure {
            statsService.getStatsForTimeSpanCountry(hashtag, startDate, endDate, topics, userId)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Suppress("LongParameterList")
    @Operation(summary = "Returns live summary statistics grouped into h3 cells for one userId")
    @GetMapping("/stats/user/h3", produces = ["text/csv", "application/json"])
    fun statsByH3andUserId(

        @Parameter(description = "OSM user id")
        @RequestParam("userId")
        userId: String,

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
        countries: List<String>,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?
    ): String {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token")
        }

        return statsService.getStatsByH3(hashtag, startDate, endDate, topic, resolution, countries, userId)
    }
}
