package org.heigit.ohsome.stats

import org.heigit.ohsome.stats.utils.HashtagHandler
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant


@WebMvcTest(StatsController::class)
class StatsControllerMVCTests {

    private val hashtag = "&uganda"


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
        `when`(repo.getStatsForTimeSpan(any(HashtagHandler::class.java), any(), any(), anyBoolean()))
            .thenReturn(mapOf("hashtag" to hashtag))

        this.mockMvc
            .perform(get("/stats/$hashtag"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.hashtag").value(hashtag))
    }

    @Test
    fun `stats with ohsomeFormat returns result object with ohsome formated metadata`() {
        `when`(repo.getStatsForTimeSpan(any(HashtagHandler::class.java), any(), any(), anyBoolean()))
            .thenReturn(mapOf("hashtag" to hashtag))

        this.mockMvc
            .perform(get("/stats/$hashtag?ohsomeFormat=true"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/&uganda?ohsomeFormat=true"))
    }

    @Test
    fun `stats per interval can be served with explicit start and end dates`() {
        `when`(
            repo.getStatsForTimeSpanInterval(
                any(HashtagHandler::class.java),
                any(Instant::class.java),
                any(Instant::class.java),
                anyString(),
                anyBoolean()
            )
        )
            .thenReturn(listOf(mapOf("hashtag" to hashtag)))

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")

        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.query.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/&uganda/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M")
            )
    }

    @Test
    fun `stats_static should return a static map of stats values`() {

        this.mockMvc
            .perform(get("/stats_static"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedStatic, false))
    }

    @Test
    fun `stats per interval and country can be served with explicit start and end date`() {
        `when`(
            repo.getStatsForTimeSpanCountry(
                any(HashtagHandler::class.java),
                any(Instant::class.java),
                any(Instant::class.java),
                anyBoolean(),
            )
        )
            .thenReturn(listOf(mapOf("country" to "xyz")))

        val GET = get("/stats/$hashtag/country")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")

        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.query.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/&uganda/country?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z")
            )
            .andExpect(jsonPath("$.result").value(mapOf("country" to "xyz")))
    }

    @Test
    fun `metadata should return max_timestamp and min_timestamp`() {
        `when`(repo.getMetadata(anyBoolean()))
            .thenReturn(mapOf("max_timestamp" to "2021-12-09T13:01:28"))

        this.mockMvc
            .perform(get("/metadata"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.max_timestamp").value("2021-12-09T13:01:28"))
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
