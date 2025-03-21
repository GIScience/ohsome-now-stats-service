package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.SpringTestWithClickhouse
import org.heigit.ohsome.now.statsservice.WithTopicData
import org.heigit.ohsome.now.statsservice.createClickhouseContainer
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import java.time.Instant
import java.time.LocalDateTime


@SpringTestWithClickhouse
@WithTopicData
class TopicRepoIntegrationTests {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = createClickhouseContainer()


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }

    @Autowired
    lateinit var repo: TopicRepo

    val topic = "place"


    val expected = mapOf(
        "topic_result" to 5,
        "topic_result_created" to 9,
        "topic_result_modified" to 16,
        "topic_result_deleted" to 4,
        "hashtag" to "hotmicrogrant"
    )


    private val emptyListCountryHandler = CountryHandler(emptyList())
    val bolivia = CountryHandler(listOf("BOL"))
    val liberia = CountryHandler(listOf("LBR"))


    @Test
    fun `getTopicStatsForTimeSpan should return all data when using no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result =
            this.repo.getTopicStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler, TopicHandler(topic))

        println(result)
        assertEquals(5, result.size)
        assertEquals(expected.toString(), result.toString())
    }


    @Test
    fun `getTopicStatsForTimeSpan should return all data for topic with two key columns`() {
        val hashtagHandler = HashtagHandler("adt")
        val result =
            this.repo.getTopicStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                TopicHandler("healthcare")
            )

        println(result)
        assertEquals(5, result.size)
        assertEquals("-3", result["topic_result"].toString())
    }

    @Test
    fun `getTopicStatsForTimeSpan should return all data for topic with length aggregation`() {
        val hashtagHandler = HashtagHandler("hotmicrogrants*")
        val result =
            this.repo.getTopicStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                TopicHandler("waterway")
            )

        println(result)
        assertEquals(7, result.size)
        assertEquals("1.206", result["topic_result"].toString())
    }

    @Test
    fun `getTopicStatsForTimeSpan should return partial data for given end date and single country for topic without value restriction`() {

        val hashtagHandler = HashtagHandler("hotosm-project-osmliber*")
        val endDate = Instant.ofEpochSecond(1496605067)

        val result =
            this.repo.getTopicStatsForTimeSpan(
                hashtagHandler,
                null,
                endDate,
                liberia,
                TopicHandler("amenity")
            )

        println(result)
        assertEquals(5, result.size)
        assertEquals("19", result["topic_result"].toString())
    }


    @Test
    fun `getTopicStatsForTimeSpanInterval returns partial topic data in time span for start and end date with hashtag aggregated by month without countries`() {

        val startDate = Instant.ofEpochSecond(1420991470)

        val endDate = Instant.ofEpochSecond(1640054890)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            this.emptyListCountryHandler,
            TopicHandler(topic)
        )

        println(result)
        assertEquals(6, result.size)
        assertEquals(84, (result["topic_result"] as DoubleArray).size)
        assertEquals("2015-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())

        // 3 new places at the beginning of the interval
        assertEquals("3.0", (result["topic_result"] as DoubleArray)[0].toString())
        assertEquals("2.0", (result["topic_result"] as DoubleArray)[35].toString())
    }


    @Test
    fun `getTopicStatsForTimeSpanInterval returns partial topic data in time span for start and end date with hashtag aggregated by month with 1 country`() {

        val startDate = Instant.ofEpochSecond(1420991470)

        val endDate = Instant.ofEpochSecond(1640054890)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            bolivia,
            TopicHandler(topic)
        )

        println(result)
        assertEquals(6, result.size)
        assertEquals(84, (result["topic_result"] as DoubleArray).size)

        assertEquals("2015-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())

        // 3 new places in 'BRA' at the beginning of the interval but countries are restricted to 'BOL'
        assertEquals("0.0", (result["topic_result"] as DoubleArray)[0].toString())
        assertEquals("2.0", (result["topic_result"] as DoubleArray)[35].toString())
    }


    @Test
    fun `getTopicStatsForTimeSpanInterval returns all data when nothing is supplied as startdate`() {
        val startDate = null

        val endDate = Instant.ofEpochSecond(1639054888)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            this.emptyListCountryHandler,
            TopicHandler(topic)
        )

        println(result)
        assertEquals(6, result.size)
        assertEquals(624, (result["topic_result"] as DoubleArray).size)
        assertEquals("1970-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())

        assertEquals("3.0", (result["topic_result"] as DoubleArray)[540].toString())
        assertEquals("2.0", (result["topic_result"] as DoubleArray)[575].toString())
    }


    @Test
    fun `getTopicStatsForTimeSpanInterval fills data between two dates with zeros`() {

        val startDate = Instant.ofEpochSecond(1503644723)

        val endDate = Instant.ofEpochSecond(1640486233)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            this.emptyListCountryHandler,
            TopicHandler(topic)
        )


        assertEquals(6, result.size)
        assertEquals(53, (result["startdate"] as Array<LocalDateTime>).size)


        (result["topic_result"] as DoubleArray).forEach() {
            assertNotNull(it)
        }

        assertEquals("2017-08-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
    }


    @Test
    fun `getTopicStatsForTimeSpanCountry returns partial data in time span for start and end date with hashtag aggregated by month and country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getTopicStatsForTimeSpanCountry(hashtagHandler, startDate, endDate, TopicHandler(topic))

        println(result)
        result.forEachIndexed { counter, it -> println(" $counter $it") }
        assertEquals(2, result.size)
        assertEquals(5, result[0].size)

        assertEquals(2L, result[0]["topic_result"])
        assertEquals("BOL", result[0]["country"])

        assertEquals(2L, result[1]["topic_result"])
        assertEquals("BRA", result[1]["country"])
    }

    @Test
    fun `getStatsForUserIdForAllHotTMProjects returns stats for only one userid`() {
        val result = this.repo.getTopicForUserIdForAllHotTMProjects(
            "4362353",
            TopicHandler(topic),
            HashtagHandler("hotosm-project-*")
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(-1L, result["topic_result"])
        assertEquals(5, result.size)
    }

    @Test
    fun `getStatsForUserIdForAllHotTMProjects returns zeros for unavailable user id`() {
        val result = this.repo.getTopicForUserIdForAllHotTMProjects(
            "2381",
            TopicHandler(topic),
            HashtagHandler("hotosm-project-*")
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(2381, result["user_id"])
        assertEquals(0.0, result["topic_result"])
    }

    @Test
    fun `getStatsForUserIdForAllHotTMProjects returns values for different hashtag`() {
        val result = this.repo.getTopicForUserIdForAllHotTMProjects(
            "6791950",
            TopicHandler("building"),
            HashtagHandler("&uganda")
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(6791950, result["user_id"])
        assertEquals(1L, result["topic_result"])
    }
}


