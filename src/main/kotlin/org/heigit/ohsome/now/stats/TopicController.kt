package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.models.OhsomeFormat
import org.heigit.ohsome.now.stats.models.StatsResult
import org.heigit.ohsome.now.stats.models.TopicResult
import org.heigit.ohsome.now.stats.models.buildOhsomeFormat
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

}