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
class NotCachingTests {


    val ugandaHashtag = "uganda"
    val ugandaHashtagHandler = HashtagHandler(ugandaHashtag)


    val noCountries = CountryHandler(emptyList())
    val buildingTopic = TopicHandler("building")
    val highwayTopic = TopicHandler("highway")



    private val exampleTopicData: Map<String, Any> = mapOf(
        "hashtag" to ugandaHashtag,
        "topic_result" to UnsignedLong.valueOf(20L),
        "topic_result_modified" to UnsignedLong.valueOf(0L),
        "topic_result_created" to UnsignedLong.valueOf(20L),
        "topic_result_deleted" to UnsignedLong.valueOf(0L)
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


    fun serviceCall(hashtag: String): () -> StatsResult = { statsService.getStatsForTimeSpan(hashtag, null, null, emptyList()) }


    @Test
    fun `stats are NOT cached if hashtag does NOT match 'hotosm-'`() {

        setupMockingForRepo()

        //calls with non-hotosm hashtag must NEVER be cached
        assertTotalNumberOfCallsToRepo(serviceCall(ugandaHashtag), 1)
        assertTotalNumberOfCallsToRepo(serviceCall(ugandaHashtag), 2)
        assertTotalNumberOfCallsToRepo(serviceCall(ugandaHashtag), 3)

    }



    private fun setupMockingForRepo() {

        //hashtag uganda
        `when`(this.statsRepo.getStatsForTimeSpan(ugandaHashtagHandler, null, null, noCountries))
            .thenReturn(exampleStatsData)

        `when`(this.topicRepo.getTopicStatsForTimeSpan(ugandaHashtagHandler, null, null, noCountries, buildingTopic))
            .thenReturn(exampleTopicData)

        `when`(this.topicRepo.getTopicStatsForTimeSpan(ugandaHashtagHandler, null, null, noCountries, highwayTopic))
            .thenReturn(exampleTopicData)
    }


    private fun assertTotalNumberOfCallsToRepo(call: () -> StatsResult, callCount: Int) {
        assertEquals("213124", call().edits.toString())
        verify(this.statsRepo, times(callCount))
            .getStatsForTimeSpan(ugandaHashtagHandler, null, null, noCountries)
    }



}
