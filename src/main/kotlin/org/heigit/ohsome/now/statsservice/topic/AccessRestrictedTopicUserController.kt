package org.heigit.ohsome.now.statsservice.stats

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.AppProperties
import org.heigit.ohsome.now.statsservice.OhsomeFormat
import org.heigit.ohsome.now.statsservice.buildOhsomeFormat
import org.heigit.ohsome.now.statsservice.measure
import org.heigit.ohsome.now.statsservice.topic.TopicService
import org.heigit.ohsome.now.statsservice.topic.UserTopicResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException


@CrossOrigin
@RestController
class AccessRestrictedTopicUserController {


    @Autowired
    lateinit var topicService: TopicService


    @Autowired
    lateinit var appProperties: AppProperties


    @Operation(summary = "Returns aggregated HOT-TM-project topic statistics for a specific user.")
    @GetMapping("/hot-tm-user/topics/{topics}", produces = ["application/json"])
    fun topicHotTMUser(
        httpServletRequest: HttpServletRequest,

        @Parameter(description = "OSM user id")
        @RequestParam(name = "userId")
        userId: String,

        @RequestHeader(value = "Authorization", required = false)
        authorization: String?,

        @Parameter(description = "Topics for which stats are to be generated e.g. 'place'")
        @PathVariable
        topics: List<String>
    ): OhsomeFormat<Map<String, UserTopicResult>> {
        if (authorization == null || authorization != "Basic ${appProperties.token}") {
            throw ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        
        val result = measure {
            topicService.getTopicsForUserIdForAllHotTMProjects(userId, topics)
        }

        return buildOhsomeFormat(result, httpServletRequest)
    }


}