package org.heigit.ohsome.stats

import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@WebMvcTest(StatsController::class)
class StatsControllerTests {


    @MockBean
    private lateinit var repo: StatsRepo


    @Autowired
    private lateinit var mockMvc: MockMvc


    //language=JSON
    private val expectedStatic = """{
          "changesets": 65009011,
          "users": 3003842,
          "roads": 45964973.0494135,
          "buildings": 844294167,
          "edits": 1095091515,
          "latest": "2023-03-20T10:55:38.000Z",
          "hashtag": "*"
      }"""


    @Test
    fun `stats can be served without date restriction`() {

        `when`(repo.getStats("&uganda"))
            .thenReturn(mapOf("hashtag" to "*"))

        this.mockMvc
            .perform(get("/stats/&uganda"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().string(containsString(""""hashtag":""")))
    }


    //TODO: separate tests for startdate and enddate combinations
    @Test
    fun `stats can be served with explicit start date`() {

        `when`(repo.getStats("&uganda"))
            .thenReturn(mapOf("hashtag" to "*"))

        val GET = get("/stats/&uganda")
            .queryParam("startdate", "2017-10-01T04:00+05:00")
            .queryParam("enddate", "2020-10-01T04:00+00:00")


        //TODO: use json to validate instead of string

        this.mockMvc
            .perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().string(containsString(""""hashtag":""")))
            .andExpect(content().string(containsString(""""startdate"""")))
            .andExpect(content().string(containsString(""""2017-10-01T04:00:00+05:00"""")))
            .andExpect(content().string(containsString(""""2020-10-01T04:00:00Z"""")))
    }


    @Test
    fun `stats_static should return a static map of stats values`() {

        this.mockMvc
            .perform(get("/stats_static"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedStatic, false))

    }

}
