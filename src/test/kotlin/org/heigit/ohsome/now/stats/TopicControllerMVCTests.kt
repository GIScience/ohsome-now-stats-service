package org.heigit.ohsome.now.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.stats.models.StatsResult
import org.heigit.ohsome.now.stats.models.TopicResult
import org.heigit.ohsome.now.stats.models.toStatsResult
import org.heigit.ohsome.now.stats.models.toTopicResult
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
    private var exampleTopic: TopicResult = exampleTopicData.toTopicResult(topic)


    @Test
    fun `topic can be served without explicit timespans`() {
        Mockito.`when`(
            this.topicService.getTopicStatsForTimeSpan(
                Mockito.matches(hashtag),
                Mockito.any(),
                Mockito.any(),
                Mockito.anyList(),
                Mockito.matches(topic)
            )
        )
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/topic/$topic")
                    .queryParam("hashtag", hashtag)
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.value").value(20))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.topic").value(topic))
            .andExpect(MockMvcResultMatchers.jsonPath("$.query.timespan.endDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.metadata.requestUrl").value("/topic/place?hashtag=%26uganda"))
    }

    @Test
    fun `topic can be served with explicit timespans`() {
        Mockito.`when`(
            this.topicService.getTopicStatsForTimeSpan(
                Mockito.matches(hashtag),
                Mockito.any(),
                Mockito.any(),
                Mockito.anyList(),
                Mockito.matches(topic)
            )
        )
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/topic/$topic")
                    .queryParam("hashtag", hashtag)
                    .queryParam("startdate", "2017-10-01T04:00:00Z")
                    .queryParam("enddate", "2020-10-01T04:00:00Z")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.value").value(20))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.topic").value(topic))
            .andExpect(MockMvcResultMatchers.jsonPath("$.query.timespan.endDate").value("2020-10-01T04:00:00Z"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=%26uganda&startdate=2017-10-01T04:00:00Z&enddate=2020-10-01T04:00:00Z")
            )
    }

    @Test
    fun `stats can be served without explicit timespans and a country filter`() {

        Mockito.`when`(this.topicService.getTopicStatsForTimeSpan("*", null, null, listOf("UGA", "DE"), topic))
            .thenReturn(exampleTopic)

        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/topic/$topic")
                    .queryParam("hashtag", "*")
                    .queryParam("countries", "UGA,DE")
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.result.topic").value(topic))
            .andExpect(MockMvcResultMatchers.jsonPath("$.query.timespan.endDate").exists())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.metadata.requestUrl")
                    .value("/topic/place?hashtag=*&countries=UGA,DE")
            )
    }


}