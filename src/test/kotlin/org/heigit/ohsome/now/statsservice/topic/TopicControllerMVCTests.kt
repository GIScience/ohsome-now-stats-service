package org.heigit.ohsome.now.statsservice.topic

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.anyInstant
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


@WebMvcTest(TopicController::class)
class TopicControllerMVCTests {

    @MockitoBean
    private lateinit var topicService: TopicService


    @Autowired
    private lateinit var mockMvc: MockMvc


    private val hashtag = "&uganda"
    private val topic1 = "place"
    private val topics = listOf(topic1)

    private val exampleTopicData: Map<String, Any> = mapOf(
        "hashtag" to hashtag,
        "topic_result" to UnsignedLong.valueOf(20L),
        "topic_result_modified" to UnsignedLong.valueOf(0L),
        "topic_result_created" to UnsignedLong.valueOf(20L),
        "topic_result_deleted" to UnsignedLong.valueOf(0L)
    )

    private val exampleTopic: Map<String, TopicResult> = mapOf(topic1 to exampleTopicData.toTopicResult(topic1))


    private val exampleTopicIntervalStatsData = mapOf(
        "topic_result" to doubleArrayOf(1001.0),
        "topic_result_modified" to longArrayOf(0L),
        "topic_result_created" to doubleArrayOf(1001.0),
        "topic_result_deleted" to doubleArrayOf(0.0),
        "startdate" to arrayOf(LocalDateTime.parse("2053-05-20T00:00:00")),
        "enddate" to arrayOf(LocalDateTime.parse("2067-05-20T00:00:00")),
    )

    private val exampleTopicStats = exampleTopicIntervalStatsData.toTopicIntervalResult("place")


    @Suppress("DANGEROUS_CHARACTERS")
    @ParameterizedTest
    @ValueSource(
        strings = [
            "/topic/highway,healthcare?hashtag=*",
            "/topic/highway,healthcare/interval?hashtag=*&interval=P1M",
            "/topic/highway,healthcare/country?hashtag=*",
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


    @ParameterizedTest
    @ValueSource(
        strings = [
            "/topic/badtopic?hashtag=whatever",
            "/topic/healthcare,badtopic?hashtag=whatever",
            "/topic/badtopic/interval?hashtag=whatever&interval=P1M",
            "/topic/badtopic,healthcare/interval?hashtag=whatever&interval=P1M",
            "/topic/badtopic/country?hashtag=whatever",
            "/topic/highway,badtopic/country?hashtag=whatever",
        ]
    )
    fun `all requests for invalid topics throw error`(url: String) {

        val expectedErrorMessage = """[{"message":"Topic not valid","invalidValue":"badtopic"}]"""

        val GET = get(url)

        this.mockMvc
            .perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))

    }


    @Test
    fun `topic can be served without explicit hashtag`() {
        `when`(this.topicService.getTopicStatsForTimeSpan(matches(""), any(), any(), anyList(), anyList()))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/${topics.joinToString()}")
                    .queryParam("hashtag", "")
                    .queryParam("interval", "P1M")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$topic1.value").value(20))
            .andExpect(jsonPath("$.result.$topic1.topic").value(topic1))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/topic/place?hashtag=&interval=P1M"))
    }


    @Test
    fun `topic can be served without explicit timespans`() {

        `when`(this.topicService.getTopicStatsForTimeSpan(matches(hashtag), any(), any(), anyList(), anyList()))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/${topics.joinToString()}")
                    .queryParam("hashtag", hashtag)
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$topic1.value").value(20))
            .andExpect(jsonPath("$.result.$topic1.topic").value(topic1))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/topic/place?hashtag=%26uganda"))
    }


    @Test
    fun `topic can be served with explicit timespans`() {

        `when`(this.topicService.getTopicStatsForTimeSpan(matches(hashtag), any(), any(), anyList(), anyList()))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/${topics.joinToString()}")
                    .queryParam("hashtag", hashtag)
                    .queryParam("startdate", "2017-10-01T04:00:00Z")
                    .queryParam("enddate", "2020-10-01T04:00:00Z")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$topic1.value").value(20))
            .andExpect(jsonPath("$.result.$topic1.topic").value(topic1))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=%26uganda&startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z")
            )
    }


    @Test
    fun `topic can be served without explicit timespans and a country filter`() {

        `when`(
            this.topicService.getTopicStatsForTimeSpan(
                "m*", null, null, listOf("UGA", "DE"),
                topics
            )
        )
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/${topics.joinToString()}")
                    .queryParam("hashtag", "m*")
                    .queryParam("countries", "UGA,DE")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$topic1.topic").value(topic1))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=m*&countries=UGA,DE")
            )
    }


    @Test
    fun `topic stats per interval can be served with explicit start and end dates and without countries`() {
        `when`(
            this.topicService.getTopicStatsForTimeSpanInterval(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyString(),
                anyList(),
                anyList()
            )
        ).thenReturn(mapOf(topic1 to exampleTopicStats))


        val GET = get("/topic/${topics.joinToString()}/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("hashtag", hashtag)

        val expectedUrl =
            "/topic/place/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M&hashtag=%26uganda"

        this.mockMvc.perform(GET)
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))

            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.metadata.requestUrl").value(expectedUrl))
            .andExpect(jsonPath("$.query.timespan.interval").value("P1M"))
            .andExpect(jsonPath("$.result.$topic1.value[0]").value(1001))
    }


    @Test
    fun `topic stats per interval can be served with explicit start and end dates and with countries`() {

        `when`(
            this.topicService.getTopicStatsForTimeSpanInterval(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyString(),
                anyList(),
                anyList()
            )
        ).thenReturn(mapOf(topic1 to exampleTopicStats))

        val GET = get("/topic/${topics.joinToString()}/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("countries", "UGA,DE")
            .queryParam("hashtag", hashtag)

        val expectedUrl =
            "/topic/place/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M&countries=UGA,DE&hashtag=%26uganda"

        this.mockMvc.perform(GET)
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))

            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.metadata.requestUrl").value(expectedUrl))
            .andExpect(jsonPath("$.query.topics").exists())
            .andExpect(jsonPath("$.query.hashtag").exists())
            .andExpect(jsonPath("$.query.countries").exists())
            .andExpect(jsonPath("$.result.$topic1.value[0]").value(1001))
    }


    @Test
    fun `topic stats per interval throws error for invalid interval string`() {

        val expectedErrorMessage = """[{"message":"Invalid ISO8601 string as interval.","invalidValue":"bad_interval"}]"""


        val GET = get("/topic/${topics.joinToString()}/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "bad_interval")
            .queryParam("hashtag", "missingmaps")


        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))
    }


    @Test
    fun `topic stats per interval throws error for interval under one Minute`() {

        val expectedErrorMessage = """[{"message":"Interval must not be under 1 minute.","invalidValue":"PT1S"}]"""


        val GET = get("/topic/${topics.joinToString()}/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "PT1S")
            .queryParam("hashtag", "missingmaps")


        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
            .andExpect(content().string(expectedErrorMessage))

    }


    @Test
    fun `topic stats per country can be served with explicit start and end date`() {

        val result1 = TopicCountryResult("place", 444.0, ModifiedSection(0L, null, null), 0.0, 444.0, "BOL")
        val result2 = TopicCountryResult("place", 333.0, ModifiedSection(0L, null, null), 0.0, 333.0, "BRA")
        val result = mapOf(topic1 to listOf(result1, result2))

        `when`(this.topicService.getTopicStatsForTimeSpanCountry(anyString(), anyInstant(), anyInstant(), anyList()))
            .thenReturn(result)

        val GET = get("/topic/${topics.joinToString()}/country")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("hashtag", hashtag)
        println("/topic/${topics.joinToString()}/country")
        val expectedURL =
            "/topic/place/country?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&hashtag=%26uganda"

        this.mockMvc.perform(GET)
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))

            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.metadata.requestUrl").value(expectedURL))
            .andExpect(jsonPath("$.result.$topic1.[0].country").value("BOL"))
    }
}
