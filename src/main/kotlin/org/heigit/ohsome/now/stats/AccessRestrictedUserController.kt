package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.models.UserResult
import org.heigit.ohsome.now.stats.models.buildUserResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import kotlin.random.Random

import kotlin.system.measureTimeMillis

@Suppress("largeClass")
@CrossOrigin
@RestController
class AccessRestrictedUserController {

    @Autowired
    lateinit var repo: StatsRepo

    @Autowired
    lateinit var appProperties: AppProperties


    @Operation(summary = "Returns aggregated HOT-TM-project statistics for a specific user.")
    @GetMapping("/hot-tm-user", produces = ["application/json"])
    fun statsHotTMUser(
        httpServletRequest: HttpServletRequest,
        @Parameter(description = "OSM user id")
        @RequestParam(name = "userId")
        userId: String,
        @RequestHeader(value = "Authorization", required = false)
        authorization: String?
    ): UserResult {
        println(appProperties.token)
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        val response = repo.getStatsForUserIdForAllHotTMProjects(userId)
        response["building_count"] = Random.nextInt(1, 100)
        response["road_length"] = Random.nextDouble(1.0, 1000.0)
        response["object_edits"] = Random.nextInt(100, 2000)
        return buildUserResult(response)
    }

}
