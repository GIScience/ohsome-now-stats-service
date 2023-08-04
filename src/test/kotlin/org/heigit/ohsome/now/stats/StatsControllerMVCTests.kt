package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.HashtagHandler
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
    
    @Test
    fun `stats can be served without date restriction`() {
        `when`(repo.getStatsForTimeSpan(any(HashtagHandler::class.java), any(), any()))
            .thenReturn(mapOf("hashtag" to hashtag))

        this.mockMvc
            .perform(get("/stats/$hashtag"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.hashtag").value(hashtag))
    }

    @Test
    fun `stats with ohsomeFormat returns result object with ohsome formated metadata`() {
        `when`(repo.getStatsForTimeSpan(any(HashtagHandler::class.java), any(), any()))
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
    fun `stats can be served with multiple hashtags`() {
        `when`(repo.getStatsForTimeSpan(any(HashtagHandler::class.java), any(), any()))
            .thenReturn(mapOf("hashtag" to hashtag))

        this.mockMvc
            .perform(get("/stats/$hashtag,hotosm*"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.$hashtag.hashtag").value(hashtag))
    }

    @Test
    fun `stats can be served with multiple hashtags and with ohsomeFormat`() {
        `when`(repo.getStatsForTimeSpan(any(HashtagHandler::class.java), any(), any()))
            .thenReturn(mapOf("hashtag" to hashtag))

        this.mockMvc
            .perform(get("/stats/$hashtag,hotosm*?ohsomeFormat=true"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$hashtag.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/&uganda,hotosm*?ohsomeFormat=true"))
    }


    @Test
    fun `stats per interval can be served with explicit start and end dates`() {
        `when`(
            repo.getStatsForTimeSpanInterval(
                any(HashtagHandler::class.java),
                any(Instant::class.java),
                any(Instant::class.java),
                anyString()
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
    fun `stats per interval and country can be served with explicit start and end date`() {
        `when`(
            repo.getStatsForTimeSpanCountry(
                any(HashtagHandler::class.java),
                any(Instant::class.java),
                any(Instant::class.java),
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
        `when`(repo.getMetadata())
            .thenReturn(mapOf("max_timestamp" to "2021-12-09T13:01:28"))

        this.mockMvc
            .perform(get("/metadata"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.max_timestamp").value("2021-12-09T13:01:28"))
    }

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
