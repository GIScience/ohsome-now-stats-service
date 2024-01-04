package org.heigit.ohsome.now.statsservice.topic

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.*
import org.heigit.ohsome.now.statsservice.utils.validateIntervalString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant


@CrossOrigin
@RestController
class TopicController {

    @Autowired
    lateinit var topicService: TopicService


    @Suppress("LongParameterList")
    @Operation(summary = "Get stats for a list of topics")
    @GetMapping("/topic/{topics}", produces = ["application/json"])
    fun topic(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @RequestParam("hashtag")
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

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<String>

    ): OhsomeFormat<Map<String, TopicResult>> {

        validateTopics(topics)

        val result = measure {
            topicService.getTopicStatsForTimeSpan(hashtag, startDate, endDate, countries!!, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by a given time interval for a list of topics")
    @GetMapping("/topic/{topics}/interval", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun topicInterval(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @RequestParam("hashtag")
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
        countries: List<String>?,

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<String>
    ): OhsomeFormat<Map<String, List<TopicIntervalResult>>> {

        validateTopics(topics)
        validateIntervalString(interval)

        val result = measure {
            topicService.getTopicStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries!!, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by country for a list of topics")
    @GetMapping("/topic/{topics}/country", produces = ["application/json"])
    @Suppress("LongParameterList")
    fun topicCountry(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'")
        @RequestParam("hashtag")
        hashtag: String,

        @StartDateConfig
        @RequestParam(name = "startdate", required = false)
        startDate: Instant?,

        @EndDateConfig
        @RequestParam(name = "enddate", required = false)
        endDate: Instant?,

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<String>
    ): OhsomeFormat<Map<String, List<TopicCountryResult>>> {

        validateTopics(topics)

        val result = measure {
            topicService.getTopicStatsForTimeSpanCountry(hashtag, startDate, endDate, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    //TODO: consider replacing with jakarta bean validation annotations instead of throwing exception
    private fun validateTopics(topics: List<String>) {
        if (!areTopicsValid(topics))
            throw ResponseStatusException(BAD_REQUEST)
    }


}