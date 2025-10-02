package org.heigit.ohsome.now.statsservice.topic

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.OhsomeFormat
import org.heigit.ohsome.now.statsservice.buildOhsomeFormat
import org.heigit.ohsome.now.statsservice.measure
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@CrossOrigin
@RestController
@Validated
class TopicController {

    @Autowired
    lateinit var topicService: TopicService

    @Operation(summary = "Return definition for a list of topics")
    @GetMapping("/topic/definition", produces = ["application/json"])
    fun topicDefinition(
        @Parameter(
            description = """
                Topics for which definition is to be returned e.g. 'place'.
                No input results in all available topic definitions
            """
        )
        @RequestParam(name = "topics", required = false)
        topics: List<@ValidTopic String>?,

        httpServletRequest: HttpServletRequest,
    ): OhsomeFormat<Map<String, String>> {
        val result = measure {
            topicService.getTopicDefinitions(topics)
        }
        return buildOhsomeFormat(result, httpServletRequest)
    }
}
