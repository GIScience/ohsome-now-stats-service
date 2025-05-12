package org.heigit.ohsome.now.statsservice.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@CrossOrigin
@RestController
class AccessRestrictedUserController {


    @Autowired
    lateinit var statsService: StatsService


    @Autowired
    lateinit var appProperties: AppProperties


    @Operation(summary = "Returns aggregated statistics for a specific user.")
    @GetMapping("/stats/user/{userId}", produces = ["application/json"])
    fun statsByUserId(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "OSM user id")
        @PathVariable
        userId: String,

        @Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'", example = "hotosm-project-*")
        @RequestParam("hashtag")
        @ValidHashtag
        hashtag: String,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?
    ): OhsomeFormat<UserResult> {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Token")
        }

        val result = measure {
            statsService.getStatsByUserId(userId, hashtag)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }
}
