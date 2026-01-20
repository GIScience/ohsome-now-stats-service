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

        @TopicsConfig
        @RequestParam("topics", required = true)
        topics: List<@ValidTopic String>
    ): OhsomeFormat<UserResult> {
        val result = measure {
            statsService.getStatsByUserId(userId, hashtag, topics, startDate, endDate)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }
}
