package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.AppProperties
import org.heigit.ohsome.now.statsservice.topic.toTopicResult
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
class AccessRestrictedUserControllerMVCTests {
    @MockitoBean
    lateinit var statsService: StatsService


    @Autowired
    private lateinit var mockMvc: MockMvc


    @Autowired
    lateinit var appProperties: AppProperties


    val userId = "12312"


    val fakeResult = mapOf(
        "edit" to mapOf("topic_result" to UnsignedLong.valueOf(34L), "user_id" to 4324).toTopicResult("edit"),
        "changeset" to mapOf(
            "topic_result" to UnsignedLong.valueOf(2L),
        ).toTopicResult("changeset")
    ).toStatsResult()


    @Test
    fun `statsByUserId returns stats for one user only`() {

        `when`(
            statsService.getStatsForTimeSpan(
                "hotosm-project-*",
                null,
                null,
                emptyList(),
                listOf("edit", "contributor"),
                this.userId
            )
        )
            .thenReturn(fakeResult)

        val GET = get("/stats/user")
            .queryParam("userId", userId)
            .queryParam("hashtag", "hotosm-project-*")
            .queryParam("topics", "edit,contributor")
            .header("Authorization", "Basic ${appProperties.token}")


        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
    }


    @Test
    fun `statsByUserId returns forbidden without token`() {

        // service must never be called because auth happens before service invocation
        verify(statsService, never())
            .getStatsForTimeSpan(anyString(), any(), any(), anyList(), anyList(), anyString())

        val GET = get("/stats/user")
            .queryParam("userId", userId)
            .queryParam("hashtag", "hotosm-project-*")
            .queryParam("topics", "building,road")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }


    @Test
    fun `statsByUserId returns forbidden with wrong token`() {

        // service must never be called because auth happens before service invocation
        verify(statsService, never())
            .getStatsForTimeSpan(anyString(), any(), any(), anyList(), anyList(), anyString())

        val GET = get("/stats/user")
            .queryParam("userId", userId)
            .queryParam("hashtag", "hotosm-project-*")
            .queryParam("topics", "edit,changeset")
            .header("Authorization", "Basic badToken")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }
}
