package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.anyInstant
import org.heigit.ohsome.now.statsservice.topic.TopicCountryResult
import org.heigit.ohsome.now.statsservice.topic.TopicResult
import org.heigit.ohsome.now.statsservice.topic.toTopicIntervalResult
import org.heigit.ohsome.now.statsservice.topic.toTopicResult
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
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.time.OffsetDateTime


@WebMvcTest(StatsController::class)
class StatsControllerMVCTests {

    private val hashtag = "&uganda"


    @MockitoBean
    private lateinit var statsService: StatsService


    @Autowired
    private lateinit var mockMvc: MockMvc


    private var exampleStatsData: Map<String, TopicResult> = mapOf(
        "contributor" to mapOf("topic_result" to 1001.0).toTopicResult("contributor"),
        "road" to mapOf("topic_result" to 43534.5, "topic_result_modified" to 43534).toTopicResult("road"),
        "building" to mapOf("topic_result" to 123.0, "topic_result_modified" to 123).toTopicResult("building"),
        "edit" to mapOf("topic_result" to 213124.0).toTopicResult("edit"),
        "changeset" to mapOf("topic_result" to 2.0).toTopicResult("changeset"),
    )

    private var exampleStats: StatsResultWithTopics = exampleStatsData.toStatsResult()

    private var exampleIntervalStats = StatsIntervalResultWithTopics(
        arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
        arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
        mutableMapOf(
            "contributor" to mapOf(
                "topic_result" to doubleArrayOf(1001.0),
                "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
                "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
            ).toTopicIntervalResult("contributor"),
            "road" to mapOf(
                "topic_result" to doubleArrayOf(43534.5),
                "topic_result_modified" to longArrayOf(43534),
                "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
                "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
            ).toTopicIntervalResult("road"),
            "building" to mapOf(
                "topic_result" to doubleArrayOf(123.0),
                "topic_result_modified" to longArrayOf(123),
                "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
                "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
            ).toTopicIntervalResult("building"),
            "edit" to mapOf(
                "topic_result" to doubleArrayOf(213124.0),
                "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
                "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
            ).toTopicIntervalResult("edit"),
            "changeset" to mapOf(
                "topic_result" to doubleArrayOf(1.0),
                "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
                "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
            ).toTopicIntervalResult("changeset")
        )
    )

    @Suppress("DANGEROUS_CHARACTERS")
    @ParameterizedTest
    @ValueSource(
        strings = [
            "/stats?hashtag=*&topics=building",
            "/stats/hashtags/*,hotosm*",
            "/stats/hashtags/hotosm*,*",
            "/stats/hashtags/a,*,b",
            "/stats/interval?hashtag=*&interval=P1M&topics=building",
            "/stats/country?hashtag=*&&topics=building"
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

    @Suppress("DANGEROUS_CHARACTERS")
    @ParameterizedTest
    @ValueSource(
        strings = [
            "/stats?hashtag=whatever&topics=wrooong",
            "/stats/interval?hashtag=whatever&interval=P1M&topics=wrooong",
            "/stats/country?hashtag=whatever&&topics=wrooong",
        ]
    )
    fun `all requests with invalid topic name throw error`(url: String) {

        val expectedErrorMessage = """[{"message":"Topic not valid","invalidValue":"wrooong"}]"""

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
        `when`(this.statsService.getStatsForTimeSpan(matches(hashtag), any(), any(), anyList(), anyList(), anyString()))
            .thenReturn(exampleStats)

        this.mockMvc
            .perform(get("/stats").queryParam("hashtag", hashtag).queryParam("topics", "building"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.topics.building.value").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect { jsonPath("$.metadata.requestUrl").value("/stats?hashtag=&uganda") }
    }


    @Test
    fun `stats can be served without explicit timespans and a country filter`() {

        `when`(this.statsService.getStatsForTimeSpan("h*", null, null, listOf("UGA", "DE"), listOf("building")))
            .thenReturn(exampleStats)

        this.mockMvc
            .perform(
                get("/stats")
                    .queryParam("hashtag", "h*")
                    .queryParam("countries", "UGA,DE")
                    .queryParam("topics", "building")

            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.topics.building.value").value(123))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/stats?hashtag=h*&countries=UGA,DE&topics=building"))
    }


    @Test
    fun `stats for multiple hashtags can be served`() {
        `when`(this.statsService.getStatsForTimeSpanAggregate(anyList(), any(), any()))
            .thenReturn(
                mapOf(
                    this.hashtag to mapOf(
                        "users" to UnsignedLong.valueOf(1001L),
                        "roads" to 43534.5,
                        "buildings" to 123L,
                        "edits" to UnsignedLong.valueOf(213124L),
                        "latest" to OffsetDateTime.parse("2023-06-29T12:50Z"),
                        "changesets" to UnsignedLong.valueOf(2)
                    ).toStatsResult()
                )
            )

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
                anyList(),
                anyList(),
                anyString()
            )
        )
            .thenReturn(
                exampleIntervalStats
            )


        val GET = get("/stats/interval")
            .queryParam("hashtag", hashtag)
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("topics", "changeset,road,contributor,building,edit")


        fun performGetRequest(requestBuilder: MockHttpServletRequestBuilder): ResultActions {
            val result = this.mockMvc.perform(requestBuilder)
            result.andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.query.hashtag").value(hashtag))
                .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
                .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
                .andExpect(jsonPath("$.query.hashtag").value("&uganda"))
                .andExpect(jsonPath("$.result.topics.changeset.value[0]").value(1))
                .andExpect(jsonPath("$.result.topics.contributor.value[0]").value(1001))
                .andExpect(jsonPath("$.result.topics.road.value[0]").value(43534.5))
                .andExpect(jsonPath("$.result.topics.building.value[0]").value(123))
                .andExpect(jsonPath("$.result.topics.edit.value[0]").value(213124))
            return result
        }

        performGetRequest(GET)
            .andExpect {
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/interval?hashtag=uganda&startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M")
            }
    }


    @Test
    fun `stats per interval can be served with explicit start and end dates and with countries`() {

        `when`(
            this.statsService.getStatsForTimeSpanInterval(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyString(),
                anyList(),
                anyList(),
                anyString()
            )
        )
            .thenReturn(exampleIntervalStats)

        val GET = get("/stats/interval")
            .queryParam("hashtag", hashtag)
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("countries", "UGA,DE")
            .queryParam("topics", "changeset,road,contributor,building,edit")

        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.query.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/interval?hashtag=%26uganda&startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M&countries=UGA,DE&topics=changeset,road,contributor,building,edit")
            )
            .andExpect(jsonPath("$.query.hashtag").value("&uganda"))
            .andExpect(jsonPath("$.result.topics.changeset.value[0]").value(1))
            .andExpect(jsonPath("$.result.topics.contributor.value[0]").value(1001))
            .andExpect(jsonPath("$.result.topics.road.value[0]").value(43534.5))
            .andExpect(jsonPath("$.result.topics.building.value[0]").value(123))
            .andExpect(jsonPath("$.result.topics.edit.value[0]").value(213124))
    }


    @Test
    fun `stats per interval throws error for invalid interval string`() {

        val expectedErrorMessage =
            """[{"message":"Invalid ISO8601 string as interval.","invalidValue":"bad_interval"}]"""

        val GET = get("/stats/interval")
            .queryParam("hashtag", hashtag)
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "bad_interval")
            .queryParam("topics", "building")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))

    }


    @Test
    fun `stats per interval throws error for interval under one Minute`() {

        val expectedErrorMessage = """[{"message":"Interval must not be under 1 minute.","invalidValue":"PT1S"}]"""

        val GET = get("/stats/interval")
            .queryParam("hashtag", hashtag)
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "PT1S")
            .queryParam("topics", "edit")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))

    }


    @Test
    fun `stats per country can be served with explicit start and end date`() {

        val map = exampleStatsData + mapOf("country" to "xyz")


        `when`(
            this.statsService.getStatsForTimeSpanCountry(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyList(),
                anyString()
            )
        )
            .thenReturn(
                mapOf(
                    "changeset" to listOf(
                        TopicCountryResult(
                            null,
                            null,
                            null,
                            2.0,
                            "xyz"
                        )
                    )
                ).toStatsTopicCountryResult()
            )


        val GET = get("/stats/country")
            .queryParam("hashtag", hashtag)
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("topics", "changeset")

        val result = this.mockMvc.perform(GET)
        result.andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.query.hashtag").value(hashtag))
            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.hashtag").value("&uganda"))
            .andExpect(jsonPath("$.result.topics.changeset[0].country").value("xyz"))
            .andExpect(jsonPath("$.result.topics.changeset[0].value").value(2))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/stats/country?hashtag=%26uganda&startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&topics=changeset")
            )
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
            .thenReturn(
                mapOf(
                    "max_timestamp" to OffsetDateTime.parse("2021-12-09T13:01:28Z"),
                    "min_timestamp" to OffsetDateTime.parse("2000-12-09T13:01:28Z")
                ).toMetadataResult()
            )

        this.mockMvc
            .perform(get("/metadata"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.max_timestamp").value("2021-12-09T13:01:28Z"))
    }


}
