package org.heigit.ohsome.now.stats

import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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

    @Test
    fun `statsHotTMUserStats returns stats for one user only`() {
        `when`(
            repo.getStatsForUserIdForAllHotTMProjects(
                anyString(),
            )
        ).thenReturn(mutableMapOf("building_count" to 1))

        val GET = get("/HotTMUser")
            .queryParam("userId", "12312")
            .header("Authorization", "Basic ${appProperties.token}")


        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
    }

    @Test
    fun `statsHotTMUserStats returns forbidden without token`() {
        `when`(
            repo.getStatsForUserIdForAllHotTMProjects(
                anyString(),
            )
        ).thenReturn(mutableMapOf("building_count" to 1))

        val GET = get("/HotTMUser")
            .queryParam("userId", "12312")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }

    @Test
    fun `statsHotTMUserStats returns forbidden with wrong token`() {
        `when`(
            repo.getStatsForUserIdForAllHotTMProjects(
                anyString(),
            )
        ).thenReturn(mutableMapOf("building_count" to 1))

        val GET = get("/HotTMUser")
            .queryParam("userId", "12312")
            .header("Authorization", "Basic badToken")

        this.mockMvc.perform(GET)
            .andExpect(status().isForbidden)
    }
}
