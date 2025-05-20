package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.AppProperties
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
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


    val fakeResult = mutableMapOf(
        "buildings" to 1L,
        "buildings_created" to 1L,
        "buildings_modified" to 1L,
        "buildings_deleted" to 1L,
        "roads" to 234.12,
        "roads_created" to 234.12,
        "roads_deleted" to 234.12,
        "roads_modified_longer" to 234.12,
        "roads_modified_shorter" to 234.12,
        "edits" to UnsignedLong.valueOf(34L),
        "changesets" to UnsignedLong.valueOf(2L),
        "user_id" to 4324
    ).toUserResult()


    @Test
    fun `statsByUserId returns stats for one user only`() {

        `when`(statsService.getStatsByUserId(this.userId, "hotosm-project-*"))
            .thenReturn(fakeResult)

        val GET = get("/stats/user")
            .queryParam("userId", userId)
            .queryParam("hashtag", "hotosm-project-*")
            .header("Authorization", "Basic ${appProperties.token}")


        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.buildings").value(1))
    }


    @Test
    fun `statsByUserId returns forbidden without token`() {

        // service must never be called because auth happens before service invocation
        verify(statsService, never())
            .getStatsByUserId(anyString(), anyString())

        val GET = get("/stats/user")
            .queryParam("userId", userId)
            .queryParam("hashtag", "hotosm-project-*")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }


    @Test
    fun `statsByUserId returns forbidden with wrong token`() {

        // service must never be called because auth happens before service invocation
        verify(statsService, never())
            .getStatsByUserId(anyString(), anyString())

        val GET = get("/stats/user")
            .queryParam("userId", userId)
            .queryParam("hashtag", "hotosm-project-*")
            .header("Authorization", "Basic badToken")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }
}
