package org.heigit.ohsome.now.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.stats.models.TopicResult
import org.heigit.ohsome.now.stats.models.toTopicResult
import org.heigit.ohsome.now.stats.models.topicIntervalResult
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@WebMvcTest(TopicController::class)
class TopicControllerMVCTests {

    @MockBean
    private lateinit var topicService: TopicService


    @Autowired
    private lateinit var mockMvc: MockMvc


    private val hashtag = "&uganda"
    private val topic = "place"

    private val exampleTopicData: Map<String, Any> = mapOf(
        "hashtag" to hashtag,
        "topic_result" to UnsignedLong.valueOf(20L)
    )

    private val exampleTopic: TopicResult = exampleTopicData.toTopicResult(topic)


    private val exampleTopicIntervalStatsData = mapOf(
        "topic_result" to UnsignedLong.valueOf(1001L),
        "startDate" to "20.05.2053",
        "endDate" to "20.05.2067",
    )

    private val exampleTopicStats = topicIntervalResult(exampleTopicIntervalStatsData, "place")


    @Test
    fun `topic can be served without explicit timespans`() {

        `when`(this.topicService.getTopicStatsForTimeSpan(matches(hashtag), any(), any(), anyList(), matches(topic)))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/$topic")
                    .queryParam("hashtag", hashtag)
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.value").value(20))
            .andExpect(jsonPath("$.result.topic").value(topic))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(jsonPath("$.metadata.requestUrl").value("/topic/place?hashtag=%26uganda"))
    }


    @Test
    fun `topic can be served with explicit timespans`() {

        `when`(this.topicService.getTopicStatsForTimeSpan(matches(hashtag), any(), any(), anyList(), matches(topic)))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/$topic")
                    .queryParam("hashtag", hashtag)
                    .queryParam("startdate", "2017-10-01T04:00:00Z")
                    .queryParam("enddate", "2020-10-01T04:00:00Z")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.value").value(20))
            .andExpect(jsonPath("$.result.topic").value(topic))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=%26uganda&startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z")
            )
    }


    @Test
    fun `stats can be served without explicit timespans and a country filter`() {

        `when`(this.topicService.getTopicStatsForTimeSpan("*", null, null, listOf("UGA", "DE"), topic))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/$topic")
                    .queryParam("hashtag", "*")
                    .queryParam("countries", "UGA,DE")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.topic").value(topic))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=*&countries=UGA,DE")
            )
    }


    @Test
    fun `topic stats per interval can be served with explicit start and end dates and without countries`() {

        `when`(this.topicService.getTopicStatsForTimeSpanInterval( anyString(), anyInstant(), anyInstant(), anyString(), anyList(), anyString()))
            .thenReturn(listOf(exampleTopicStats))


        val GET = get("/topic/$topic/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("hashtag", hashtag)

        val expectedUrl = "/topic/place/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M&hashtag=%26uganda"

        this.mockMvc.perform(GET)
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))

            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.metadata.requestUrl").value(expectedUrl))
            .andExpect(jsonPath("$.query.timespan.interval").value("P1M"))
            .andExpect(jsonPath("$.result.[0].value").value(1001))
    }


    @Test
    fun `topic stats per interval can be served with explicit start and end dates and with countries`() {

        `when`(this.topicService.getTopicStatsForTimeSpanInterval(anyString(), anyInstant(), anyInstant(), anyString(), anyList(), anyString()))
            .thenReturn(listOf(exampleTopicStats))

        val GET = get("/topic/$topic/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("countries", "UGA,DE")
            .queryParam("hashtag", hashtag)

        val expectedUrl = "/topic/place/interval?startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z&interval=P1M&countries=UGA,DE&hashtag=%26uganda"

        this.mockMvc.perform(GET)
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))

            .andExpect(jsonPath("$.query.timespan.startDate").value("2017-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(jsonPath("$.metadata.requestUrl").value(expectedUrl))
            .andExpect(jsonPath("$.result.[0].value").value(1001))
    }


//    @Test
    fun `topic stats per interval throws error for invalid interval string`() {

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "ErrorString")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
    }


//    @Test
    fun `topic stats per interval throws error for interval under one Minute`() {

        val GET = get("/stats/$hashtag/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "PT1S")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
    }


}