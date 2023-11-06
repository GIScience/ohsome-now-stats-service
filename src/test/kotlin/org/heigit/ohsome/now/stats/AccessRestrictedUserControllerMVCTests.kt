package org.heigit.ohsome.now.stats

import com.clickhouse.data.value.UnsignedLong
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@SpringBootTest
@AutoConfigureMockMvc
class AccessRestrictedUserControllerMVCTests {

    @MockBean
    private lateinit var repo: StatsRepo

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var appProperties: AppProperties


    val userId = "12312"


    val fakeResult: MutableMap<String, Any> = mutableMapOf(
        "buildings" to 1L,
        "roads" to 234.12,
        "edits" to UnsignedLong.valueOf(34L),
        "changesets" to UnsignedLong.valueOf(2L),
        "user_id" to 4324
    )


    @Test
    fun `statsHotTMUserStats returns stats for one user only`() {

        `when`(repo.getStatsForUserIdForAllHotTMProjects(this.userId))
            .thenReturn(fakeResult)

        val GET = get("/hot-tm-user")
            .queryParam("userId", userId)
            .header("Authorization", "Basic ${appProperties.token}")


        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.buildings").value(1))
    }


    @Test
    fun `statsHotTMUserStats returns forbidden without token`() {

        `when`(repo.getStatsForUserIdForAllHotTMProjects(this.userId))
            .thenReturn(mutableMapOf("building_count" to 1))

        val GET = get("/hot-tm-user")
            .queryParam("userId", "12312")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }


    @Test
    fun `statsHotTMUserStats returns forbidden with wrong token`() {

        `when`(repo.getStatsForUserIdForAllHotTMProjects(this.userId))
            .thenReturn(mutableMapOf("building_count" to 1))

        val GET = get("/hot-tm-user")
            .queryParam("userId", "12312")
            .header("Authorization", "Basic badToken")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }
}
