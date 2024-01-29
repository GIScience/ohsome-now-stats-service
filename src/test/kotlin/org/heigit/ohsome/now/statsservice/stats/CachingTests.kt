package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.topic.TopicHandler
import org.heigit.ohsome.now.statsservice.topic.TopicRepo
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean


@SpringBootTest
class CachingTests {

    val hashtag = "hotosm-"
    val hashtagHandler = HashtagHandler(hashtag)
    val noCountries = CountryHandler(emptyList())
    val buildingTopic = TopicHandler("building")
    val highwayTopic = TopicHandler("highway")



    private val exampleTopicData: Map<String, Any> = mapOf(
        "hashtag" to hashtag,
        "topic_result" to UnsignedLong.valueOf(20L),
        "topic_result_modified" to UnsignedLong.valueOf(0L),
        "topic_result_created" to UnsignedLong.valueOf(20L),
        "topic_result_deleted" to UnsignedLong.valueOf(0L)
    )

    val expected = mapOf(
        "topic_result" to 5,
        "topic_result_created" to 9,
        "topic_result_modified" to 16,
        "topic_result_deleted" to 4,
        "hashtag" to "hotmicrogrant"
    )


    @MockBean
    private lateinit var statsRepo: StatsRepo

    @MockBean
    private lateinit var topicRepo: TopicRepo


    @Autowired
    private lateinit var statsService: StatsService



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
    fun `stats are cached if hashtag matches 'hotosm-'`() {

        `when`(this.statsRepo.getStatsForTimeSpan(hashtagHandler, null, null, noCountries))
            .thenReturn(exampleStatsData)
        `when`(this.topicRepo.getTopicStatsForTimeSpan(hashtagHandler, null, null, noCountries, buildingTopic))
            .thenReturn(exampleTopicData)
        `when`(this.topicRepo.getTopicStatsForTimeSpan(hashtagHandler, null, null, noCountries, highwayTopic))
            .thenReturn(exampleTopicData)

        val result1 = this.statsService.getStatsForTimeSpan(hashtag, null, null, emptyList())
        assertEquals("213124", result1.edits.toString())
        verify(this.statsRepo, times(1))
            .getStatsForTimeSpan(hashtagHandler, null, null, noCountries)

        //the second service call must be cached, hence the repo is not called again
        val result2 = this.statsService.getStatsForTimeSpan(hashtag, null, null, emptyList())
        assertEquals("213124", result2.edits.toString())
        verify(this.statsRepo, times(1))
            .getStatsForTimeSpan(hashtagHandler, null, null, noCountries)

    }


}
