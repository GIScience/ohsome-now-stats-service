package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.anyInstant
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime


@WebMvcTest(StatsController::class)
class StatsControllerMVCTests {

    private val hashtag = "&uganda"


    @MockitoBean
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
        "users" to longArrayOf(1001L),
        "roads" to doubleArrayOf(43534.5),
        "buildings" to doubleArrayOf(123.0),
        "edits" to longArrayOf(213124L),
        "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
        "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
        "changesets" to longArrayOf(2)
    )

    private var exampleIntervalStats = exampleIntervalStatsData.toIntervalStatsResult()


    @Suppress("DANGEROUS_CHARACTERS")
    @ParameterizedTest
    @ValueSource(
        strings = [
            "/stats?hashtag=*",
            "/stats/*",
            "/stats/hashtags/*,hotosm*",
            "/stats/hashtags/hotosm*,*",
            "/stats/hashtags/a,*,b",
            "/stats/*/interval?interval=P1M",
            "/stats/*/country"
        ]
    )
    fun `all requests with '*' hashtag throw error`(url: String) {

        val expectedErrorMessage = """[{"message":"Hashtag must not be '*'","invalidValue":"*"}]"""

        val GET = get(url)

        this.mockMvc
            .perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))
    }


    //TODO: cover all other urls supporting start/end dates
    @Disabled("disabled for now, as the validation with proper error messaging is not yet implemented")
    @ParameterizedTest
    @ValueSource(
        strings = [
            "/stats/missingmaps?startdate=xxxxx&enddate=2020-10-01T04:00:00Z",
            "/stats/missingmaps?startdate=2020-10-01T04:00:00Z&enddate=xxxxx"
        ]
    )
    fun `all requests with bad dates throw error`(url: String) {

        val expectedErrorMessage = """[{"message":"xxxxxxx","invalidValue":"xxxxx"}]"""

        val GET = get(url)


        this.mockMvc
            .perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))
    }


    @Test
    fun `stats can be served without explicit timespans`() {
        `when`(this.statsService.getStatsForTimeSpan(matches(hashtag), any(), any(), anyList()))
            .thenReturn(exampleStats)

        this.mockMvc
            .perform(get("/stats/$hashtag"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.buildings").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/&uganda"))

        this.mockMvc
            .perform(get("/stats").queryParam("hashtag", hashtag))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.buildings").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats?hashtag=%26uganda"))
    }


    @Test
    fun `stats can be served without explicit timespans and a country filter`() {

        `when`(this.statsService.getStatsForTimeSpan("h*", null, null, listOf("UGA", "DE")))
            .thenReturn(exampleStats)

        this.mockMvc
            .perform(
                get("/stats/h*").queryParam("countries", "UGA,DE")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.buildings").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/h*?countries=UGA,DE"))

        this.mockMvc
            .perform(
                get("/stats?hashtag=h*").queryParam("countries", "UGA,DE")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.buildings").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats?hashtag=h*&countries=UGA,DE"))
    }


    @Test
    fun `stats for multiple hashtags can be served`() {
        `when`(this.statsService.getStatsForTimeSpanAggregate(anyList(), any(), any()))
            .thenReturn(mapOf(this.hashtag to exampleStats))

        this.mockMvc
            .perform(get("/stats/hashtags/$hashtag,hotosm*"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$hashtag.roads").value(43534.5))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.query.hashtags").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats/hashtags/$hashtag,hotosm*"))
    }


    @Test
    fun `stats per interval can be served with explicit start and end dates and without countries`() {

        `when`(
            this.statsService.getStatsForTimeSpanInterval(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyString(),
                anyList()
            )
        )
            .thenReturn(
                StatsIntervalResult(
                    longArrayOf(1),
                    longArrayOf(2),
                    doubleArrayOf(1.0),
                    doubleArrayOf(2.0),
                    longArrayOf(2),
                    arrayOf(LocalDateTime.parse("2017-10-01T04:00:00")),
                    arrayOf(LocalDateTime.parse("2020-10-01T04:00:00"))
                )
            )

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
            .andExpect(jsonPath("$.result.changesets[0]").value(1))
            .andExpect(jsonPath("$.result.users[0]").value(2))
            .andExpect(jsonPath("$.result.roads[0]").value(1.0))
            .andExpect(jsonPath("$.result.buildings[0]").value(2.0))
            .andExpect(jsonPath("$.result.edits[0]").value(2))
    }


    @Test
    fun `stats per interval can be served with explicit start and end dates and with countries`() {

        `when`(
            this.statsService.getStatsForTimeSpanInterval(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyString(),
                anyList()
            )
        )
            .thenReturn(exampleIntervalStats)

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
            .andExpect(jsonPath("$.result.changesets[0]").value(2))
            .andExpect(jsonPath("$.result.users[0]").value(1001))
            .andExpect(jsonPath("$.result.roads[0]").value(43534.5))
            .andExpect(jsonPath("$.result.buildings[0]").value(123))
            .andExpect(jsonPath("$.result.edits[0]").value(213124))
    }


    @Test
    fun `stats per interval throws error for invalid interval string`() {

        val expectedErrorMessage = """[{"message":"Invalid ISO8601 string as interval.","invalidValue":"bad_interval"}]"""

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "bad_interval")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))

    }


    @Test
    fun `stats per interval throws error for interval under one Minute`() {

        val expectedErrorMessage = """[{"message":"Interval must not be under 1 minute.","invalidValue":"PT1S"}]"""


        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "PT1S")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))

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


        `when`(this.statsService.getMostUsedHashtags(anyInstant(), anyInstant(), anyInt(), anyList()))
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

        `when`(this.statsService.getMostUsedHashtags(anyInstant(), anyInstant(), anyInt(), anyList()))
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
            .thenReturn(mapOf("max_timestamp" to "2021-12-09T13:01:28").toMetadataResult())

        this.mockMvc
            .perform(get("/metadata"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.max_timestamp").value("2021-12-09T13:01:28"))
    }


}
