package org.heigit.ohsome.now.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.stats.models.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant


@WebMvcTest(StatsController::class)
class StatsControllerMVCTests {

    private val hashtag = "&uganda"


    @MockBean
    private lateinit var statsService: StatsService


    @Autowired
    private lateinit var mockMvc: MockMvc


    private var exampleStatsData: Map<String, Any> = mapOf(
        "users" to UnsignedLong.valueOf(1001L),
        "roads" to 43534.5,
        "buildings" to 123L,
        "edits" to UnsignedLong.valueOf(213124L),
        "latest" to "20.05.2053",
        "changesets" to UnsignedLong.valueOf(2),
    )

    private var exampleMultipleStatsData: Map<String, Any> = exampleStatsData + mapOf("hashtag" to hashtag)

    private var exampleStats: StatsResult = exampleStatsData.toStatsResult()
    private var exampleMultipleStats: StatsResult = exampleMultipleStatsData.toStatsResult()


    private var exampleIntervalStatsData = mapOf(
        "users" to UnsignedLong.valueOf(1001L),
        "roads" to 43534.5,
        "buildings" to 123L,
        "edits" to UnsignedLong.valueOf(213124L),
        "startDate" to "20.05.2053",
        "endDate" to "20.05.2067",
        "changesets" to UnsignedLong.valueOf(2)
    )

    private var exampleIntervalStats = statsIntervalResult(exampleIntervalStatsData)


    @Test
    fun `stats can be served without explicit timespans`() {
        `when`(this.statsService.getStatsForTimeSpan(anyString(), any(), any(), anyList()))
            .thenReturn(exampleStats)

        this.mockMvc
            .perform(get("/stats/$hashtag"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.buildings").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/&uganda"))
    }


    @Test
    fun `stats can be served without explicit timespans and a country filter`() {
        `when`(this.statsService.getStatsForTimeSpan(anyString(), isNull(), isNull(), anyList()))
            .thenReturn(exampleStats)

        this.mockMvc
            .perform(
                get("/stats/*").queryParam("countries", "UGA,DE")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.buildings").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/*?countries=UGA,DE"))
    }


    @Test
    fun `stats_hashtags can be served with multiple hashtags`() {
        `when`(this.statsService.getStatsForTimeSpanAggregate(anyString(), any(), any()))
            .thenReturn(mapOf(this.hashtag to exampleStats))

        this.mockMvc
            .perform(get("/stats/hashtags/$hashtag,hotosm*"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$hashtag.roads").value(43534.5))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/hashtags/$hashtag,hotosm*"))
    }


    @Test
    fun `stats per interval can be served with explicit start and end dates and without countries`() {

        `when`(this.statsService.getStatsForTimeSpanInterval(anyString(), anyInstant(), anyInstant(), anyString(), anyList()))
            .thenReturn(listOf(exampleIntervalStats))

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")

        this.mockMvc.perform(GET)
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.query.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/&uganda/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M")
            )
            .andExpect(jsonPath("$.query.hashtag").value("&uganda"))
            .andExpect(jsonPath("$.result.[0].changesets").value(2))
            .andExpect(jsonPath("$.result.[0].users").value(1001))
            .andExpect(jsonPath("$.result.[0].roads").value(43534.5))
            .andExpect(jsonPath("$.result.[0].buildings").value(123))
            .andExpect(jsonPath("$.result.[0].edits").value(213124))
    }


    @Test
    fun `stats per interval can be served with explicit start and end dates and with countries`() {

        `when`(this.statsService.getStatsForTimeSpanInterval(anyString(), anyInstant(), anyInstant(), anyString(), anyList()))
            .thenReturn(listOf(exampleIntervalStats))

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("countries", "UGA,DE")

        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.query.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/&uganda/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M&countries=UGA,DE")
            )
            .andExpect(jsonPath("$.query.hashtag").value("&uganda"))
            .andExpect(jsonPath("$.result.[0].changesets").value(2))
            .andExpect(jsonPath("$.result.[0].users").value(1001))
            .andExpect(jsonPath("$.result.[0].roads").value(43534.5))
            .andExpect(jsonPath("$.result.[0].buildings").value(123))
            .andExpect(jsonPath("$.result.[0].edits").value(213124))

    }


    @Test
    fun `stats per interval throws error for invalid interval string`() {

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "ErrorString")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
    }


    @Test
    fun `stats per interval throws error for interval under one Minute`() {

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "PT1S")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
    }


    @Test
    fun `stats per country can be served with explicit start and end date`() {

        val map = exampleStatsData + mapOf("country" to "xyz")


        `when`(this.statsService.getStatsForTimeSpanCountry(anyString(), anyInstant(), anyInstant()))
            .thenReturn(listOf(countryStatsResult(map)))

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
            .andExpect(
                jsonPath("$.query.hashtag")
                    .value("&uganda")
            )
            .andExpect(jsonPath("$.result.[0].country").value("xyz"))
    }


    @Test
    fun `most-used-hashtags should return list of hashtags`() {

        val data = hashtagResult(mapOf("hashtag" to "testtag", "number_of_users" to UnsignedLong.valueOf(242L)))


        `when`(this.statsService.getMostUsedHashtags(anyInstant(), anyInstant(), anyInt()))
            .thenReturn(listOf(data))

        val GET = get("/most-used-hashtags")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")

        this.mockMvc
            .perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.[0].number_of_users").value(242))
    }


    @Test
    fun `most-used-hashtags does not throw an error for out of timeline`() {

        `when`(this.statsService.getMostUsedHashtags(anyInstant(), anyInstant(), anyInt()))
            .thenReturn(listOf())

        val GET = get("/most-used-hashtags")
            .queryParam("startdate", "2004-10-01T04:00:00Z")
            .queryParam("enddate", "2005-10-01T04:00:00Z")

        this.mockMvc
            .perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
    }


    @Test
    fun `metadata should return max_timestamp and min_timestamp`() {

        `when`(this.statsService.getMetadata())
            .thenReturn(mapOf("max_timestamp" to "2021-12-09T13:01:28"))

        this.mockMvc
            .perform(get("/metadata"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.max_timestamp").value("2021-12-09T13:01:28"))
    }


    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun anyInstant() = any(Instant::class.java)

}
