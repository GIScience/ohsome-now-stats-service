package org.heigit.ohsome.now.statsservice.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.AppProperties
import org.heigit.ohsome.now.statsservice.OhsomeFormat
import org.heigit.ohsome.now.statsservice.buildOhsomeFormat
import org.heigit.ohsome.now.statsservice.measure
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


    @Operation(summary = "Returns aggregated HOT-TM-project statistics for a specific user.")
    @Deprecated("Users of this endpoint should switch to topic/{topics}/user.")
    @GetMapping("/hot-tm-user", produces = ["application/json"])
    fun statsHotTMUser(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "OSM user id")
        @RequestParam(name = "userId")
        userId: String,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?
    ): OhsomeFormat<UserResult> {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        val result = measure {
            statsService.getStatsForUserIdForAllHotTMProjects(userId)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


}
