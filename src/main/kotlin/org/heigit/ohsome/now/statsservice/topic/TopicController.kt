package org.heigit.ohsome.now.statsservice.topic

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.OhsomeFormat
import org.heigit.ohsome.now.statsservice.buildOhsomeFormat
import org.heigit.ohsome.now.statsservice.measure
import org.heigit.ohsome.now.statsservice.utils.validateIntervalString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
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

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("startdate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam("enddate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDate: Instant?,

        @Parameter(description = "A comma separated list of countries, can also only be one country")
        @RequestParam("countries", required = false, defaultValue = "")
        countries: List<String>?,

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<String>

    ): OhsomeFormat<Map<String, TopicResult>> {


        //TODO: für alle endpoints anwenden
        val topicsValid = areTopicsValid(topics)
        if (!topicsValid)
            throw ResponseStatusException(BAD_REQUEST);



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

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
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

        @Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "startdate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startDate: Instant?,

        @Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
        @RequestParam(name = "enddate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        endDate: Instant?,

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<String>
    ): OhsomeFormat<Map<String, List<TopicCountryResult>>> {

        val result = measure {
            topicService.getTopicStatsForTimeSpanCountry(hashtag, startDate, endDate, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


}