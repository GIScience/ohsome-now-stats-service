package org.heigit.ohsome.now.statsservice.topic

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.anyInstant
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
        "topic_result" to UnsignedLong.valueOf(1001L),
        "topic_result_modified" to UnsignedLong.valueOf(0L),
        "topic_result_created" to UnsignedLong.valueOf(1001L),
        "topic_result_deleted" to UnsignedLong.valueOf(0L),
        "startDate" to "20.05.2053",
        "endDate" to "20.05.2067",
    )

    //private val exampleTopicStats = topicIntervalResult(exampleTopicIntervalStatsData, "place")


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
    fun `stats can be served without explicit timespans and a country filter`() {

        `when`(
            this.topicService.getTopicStatsForTimeSpan(
                "*", null, null, listOf("UGA", "DE"),
                topics
            )
        )
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                get("/topic/${topics.joinToString()}")
                    .queryParam("hashtag", "*")
                    .queryParam("countries", "UGA,DE")
            )
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(jsonPath("$.result.$topic1.topic").value(topic1))
            .andExpect(jsonPath("$.query.timespan.endDate").exists())
            .andExpect(
                jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=*&countries=UGA,DE")
            )
    }


    @Test
    fun `topic stats per interval can be served with explicit start and end dates and without countries`() {
        /*todo:
                `when`(
                    this.topicService.getTopicStatsForTimeSpanInterval(
                        anyString(),
                        anyInstant(),
                        anyInstant(),
                        anyString(),
                        anyList(),
                        anyList()
                    )
                ).thenReturn(mapOf(topic1 to listOf(exampleTopicStats)))


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
                    .andExpect(jsonPath("$.result.$topic1.[0].value").value(1001))*/
    }


    @Test
    fun `topic stats per interval can be served with explicit start and end dates and with countries`() {
        /*todo:
        `when`(
            this.topicService.getTopicStatsForTimeSpanInterval(
                anyString(),
                anyInstant(),
                anyInstant(),
                anyString(),
                anyList(),
                anyList()
            )
        ).thenReturn(mapOf(topic1 to listOf(exampleTopicStats)))

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
            .andExpect(jsonPath("$.result.$topic1.[0].value").value(1001))
         */
    }


    @Test
    fun `topic stats per interval throws error for invalid interval string`() {
        /*
        val GET = get("/topic/${topics.joinToString()}/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "ErrorString")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)

         */
    }


    @Test
    fun `topic stats per interval throws error for interval under one Minute`() {

        val GET = get("/topic/${topics.joinToString()}/interval")
            .queryParam("startdate", "2017-10-01T04:00:00Z")
            .queryParam("enddate", "2020-10-01T04:00:00Z")
            .queryParam("interval", "PT1S")

        this.mockMvc.perform(GET)
            .andExpect(status().isBadRequest)
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