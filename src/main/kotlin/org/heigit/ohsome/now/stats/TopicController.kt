package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.models.OhsomeFormat
import org.heigit.ohsome.now.stats.models.TopicIntervalResult
import org.heigit.ohsome.now.stats.models.TopicResult
import org.heigit.ohsome.now.stats.models.buildOhsomeFormat
import org.heigit.ohsome.now.stats.utils.validateIntervalString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.Instant


@CrossOrigin
@RestController
class TopicController {

    @Autowired
    lateinit var topicService: TopicService


    @Suppress("LongParameterList")
    @Operation(summary = "Get stats for a specified topic")
    @GetMapping("/topic/{topic}", produces = ["application/json"])
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

        @Parameter(description = "A topic for which stats are to be generated e.g. 'place'")
        @PathVariable
        topic: String

    ): OhsomeFormat<TopicResult> {

        val result = measure {
            topicService.getTopicStatsForTimeSpan(hashtag, startDate, endDate, countries!!, topic)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


    @Operation(summary = "Returns live summary statistics for one hashtag grouped by a given time interval")
    @GetMapping("/topic/{topic}/interval", produces = ["application/json"])
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

        @Parameter(description = "A topic for which stats are to be generated e.g. 'place'")
        @PathVariable
        topic: String
    ): OhsomeFormat<List<TopicIntervalResult>> {

        validateIntervalString(interval)

        val result = measure {
            topicService.getTopicStatsForTimeSpanInterval(hashtag, startDate, endDate, interval, countries!!, topic)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }



}