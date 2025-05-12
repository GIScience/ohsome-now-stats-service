package org.heigit.ohsome.now.statsservice.topic

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.Instant


@CrossOrigin
@RestController
@Validated
class TopicController {

    @Autowired
    lateinit var topicService: TopicService


    @Suppress("LongParameterList")
    @Operation(summary = "Get stats for a list of topics")
    @GetMapping("/topic/{topics}", produces = ["application/json"])
    fun topic(
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

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<@ValidTopic String>

    ): OhsomeFormat<Map<String, TopicResult>> {

        val result = measure {
            topicService.getTopicStatsForTimeSpan(hashtag, startDate, endDate, countries, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for a list of topics, optionally filtered by one hashtag, grouped by a given time interval")
    @GetMapping("/topic/{topics}/interval", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun topicInterval(
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

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<@ValidTopic String>
    ): OhsomeFormat<Map<String, TopicIntervalResult>> {

        val result = measure {
            topicService.getTopicStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for a list of topics, optionally filtered by one hashtag, grouped by country")
    @GetMapping("/topic/{topics}/country", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun topicCountry(
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

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<@ValidTopic String>
    ): OhsomeFormat<Map<String, List<TopicCountryResult>>> {

        val result = measure {
            topicService.getTopicStatsForTimeSpanCountry(hashtag, startDate, endDate, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


}
