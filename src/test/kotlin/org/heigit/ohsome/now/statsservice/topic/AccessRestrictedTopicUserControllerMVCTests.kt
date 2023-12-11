package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.AppProperties
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class AccessRestrictedTopicUserControllerMVCTests {

    @MockBean
    lateinit var topicService: TopicService


    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var appProperties: AppProperties

    val userId = "12312"

    final val topics = listOf("place", "healthcare")

    val fakeResult = mutableMapOf(
        topics[0] to mutableMapOf(
            "topic_result" to 1,
            "user_id" to 12312
        ).toUserTopicResult(topics[0]),
        topics[1] to mutableMapOf(
            "topic_result" to 2,
            "user_id" to 12312
        ).toUserTopicResult(topics[0])
    )

    @Test
    fun `statsHotTMUserStats returns stats for one user only`() {

        Mockito.`when`(topicService.getTopicsForUserIdForAllHotTMProjects(this.userId, listOf(topics[0])))
            .thenReturn(fakeResult)

        val GET = MockMvcRequestBuilders.get("/hot-tm-user/topics/place")
            .queryParam("userId", userId)
            .header("Authorization", "Basic ${appProperties.token}")


        this.mockMvc.perform(GET)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.${topics[0]}.value").value(1))
    }


    @Test
    fun `statsHotTMUserStats returns forbidden without token`() {

        // service must never be called because auth happens before service invocation
        verify(topicService, never())
            .getTopicsForUserIdForAllHotTMProjects(anyString(), anyList())


        val GET = MockMvcRequestBuilders.get("/hot-tm-user/topics/place")
            .queryParam("userId", "12312")

        this.mockMvc.perform(GET)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }


    @Test
    fun `statsHotTMUserStats returns forbidden with wrong token`() {
        // service must never be called because auth happens before service invocation
        verify(topicService, never())
            .getTopicsForUserIdForAllHotTMProjects(anyString(), anyList())

        val GET = MockMvcRequestBuilders.get("/hot-tm-user/topics/place")
            .queryParam("userId", "12312")
            .header("Authorization", "Basic badToken")

        this.mockMvc.perform(GET)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
}