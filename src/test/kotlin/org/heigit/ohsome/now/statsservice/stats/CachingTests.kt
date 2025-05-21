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
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.Instant
import java.time.Instant.now
import java.util.concurrent.TimeUnit.SECONDS


@SpringBootTest
class CachingTests {

    val now = now()

    final val ugandaHashtag = "uganda"
    val ugandaHashtagHandler = HashtagHandler(ugandaHashtag)

    final val hotosmHashtag = "hotosm-project-*"
    val hotosmHashtagHandler = HashtagHandler(hotosmHashtag)


    val noCountries = CountryHandler(emptyList())
    val buildingTopic = TopicHandler("building")
    val roadTopic = TopicHandler("road")


    private val exampleTopicData: Map<String, Any> = mapOf(
        "hashtag" to hotosmHashtag,
        "topic_result" to UnsignedLong.valueOf(20L),
        "topic_result_modified" to UnsignedLong.valueOf(0L),
        "topic_result_created" to UnsignedLong.valueOf(20L),
        "topic_result_deleted" to UnsignedLong.valueOf(0L)
    )


    @MockitoBean
    private lateinit var statsRepo: StatsRepo

    @MockitoBean
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


    fun serviceCall(hashtag: String, date: Instant? = null): () -> StatsResult =
        { statsService.getStatsForTimeSpan(hashtag, date, null, emptyList()) }


    @DirtiesContext
    @Test
    @Suppress("DANGEROUS_CHARACTERS")
    fun `cache gets cleared on schedule`() {
        setupMockingForRepo(hotosmHashtagHandler)
        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag), 1, hotosmHashtagHandler)

        //give the cache invalidation time to happen (it is scheduled at every second for tests)
        SECONDS.sleep(2)
        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag), 2, hotosmHashtagHandler)
    }


    // please note: there is a theoretical possibility that the following test fails
    // due to a race condition where the (once-per-second) cache invalidation
    // happens right between 2 subsequent calls to the mocked repo.
    // This is very unlikely to happen at all
    // and could be fixed by using the once-per-second cron for the cache invalidation test only,
    // by, e.g. moving it to its own class.

    @DirtiesContext
    @Test
    @Suppress("DANGEROUS_CHARACTERS")
    fun `stats are cached if hashtag matches 'hotosm-project-*' and both dates are NULL`() {

        setupMockingForRepo(hotosmHashtagHandler)

        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag), 1, hotosmHashtagHandler)

        //the second service call must be cached, hence the total number of calls stays at 1
        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag), 1, hotosmHashtagHandler)
    }


    @DirtiesContext
    @Test
    @Suppress("DANGEROUS_CHARACTERS")
    fun `stats are NOT cached if hashtag matches 'hotosm-project-*' but not all dates are NULL`() {

        setupMockingForRepo(hotosmHashtagHandler, this.now)

        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag, this.now), 1, hotosmHashtagHandler, this.now)
        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag, this.now), 2, hotosmHashtagHandler, this.now)
        assertTotalNumberOfCallsToRepo(serviceCall(hotosmHashtag, this.now), 3, hotosmHashtagHandler, this.now)
    }


    @DirtiesContext
    @Test
    fun `stats are NOT cached if hashtag does NOT match 'hotosm-'`() {

        setupMockingForRepo(ugandaHashtagHandler)

        //calls with non-hotosm hashtag must NEVER be cached
        assertTotalNumberOfCallsToRepo(serviceCall(ugandaHashtag), 1, ugandaHashtagHandler)
        assertTotalNumberOfCallsToRepo(serviceCall(ugandaHashtag), 2, ugandaHashtagHandler)
        assertTotalNumberOfCallsToRepo(serviceCall(ugandaHashtag), 3, ugandaHashtagHandler)

    }


    private fun setupMockingForRepo(hashtagHandler: HashtagHandler, date: Instant? = null) {

        //hashtag hotosm
        `when`(this.statsRepo.getStatsForTimeSpan(hashtagHandler, date, null, noCountries))
            .thenReturn(exampleStatsData)

        `when`(this.topicRepo.getTopicStatsForTimeSpan(hashtagHandler, date, null, noCountries, buildingTopic))
            .thenReturn(exampleTopicData)

        `when`(this.topicRepo.getTopicStatsForTimeSpan(hashtagHandler, date, null, noCountries, roadTopic))
            .thenReturn(exampleTopicData)

    }


    private fun assertTotalNumberOfCallsToRepo(
        call: () -> StatsResult,
        callCount: Int,
        hashtagHandler: HashtagHandler,
        date: Instant? = null
    ) {
        assertEquals("213124", call().edits.toString())
        verify(this.statsRepo, times(callCount))
            .getStatsForTimeSpan(hashtagHandler, date, null, noCountries)
    }


}
