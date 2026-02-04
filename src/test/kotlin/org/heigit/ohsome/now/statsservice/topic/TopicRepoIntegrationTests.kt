package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.SpringTestWithClickhouse
import org.heigit.ohsome.now.statsservice.WithTopicData
import org.heigit.ohsome.now.statsservice.createClickhouseContainer
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.heigit.ohsome.now.statsservice.utils.UserHandler
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
    val noUserId = "";

    private val emptyListCountryHandler = CountryHandler(emptyList())
    val bolivia = CountryHandler(listOf("BOL"))
    val liberia = CountryHandler(listOf("LBR"))


    @Test
    fun `getTopicStatsForTimeSpan should return all data when using no hashtag, no time span and null-list of countries`() {
        val expected = mapOf(
            "topic_result" to 3,
            "topic_result_created" to 9,
            "topic_result_modified" to 16,
            "topic_result_deleted" to 6,
            "hashtag" to ""
        )
        val hashtagHandler = HashtagHandler("")
        val result =
            this.repo.getTopicStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                TopicHandler(topic),
                UserHandler(noUserId)
            )

        println(result)
        assertEquals(5, result.size)
        assertEquals(expected.toString(), result.toString())
    }


    @Test
    fun `getTopicStatsForTimeSpan should return all data for wildcard hashtag when using no time span and null-list of countries`() {
        val expected = mapOf(
            "topic_result" to 5,
            "topic_result_created" to 9,
            "topic_result_modified" to 16,
            "topic_result_deleted" to 4,
            "hashtag" to "hotmicrogrant"
        )
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result =
            this.repo.getTopicStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                TopicHandler(topic),
                UserHandler(noUserId)
            )

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
                TopicHandler("healthcare"),
                UserHandler(noUserId)
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
                TopicHandler("waterway"),
                UserHandler(noUserId)
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
                TopicHandler("amenity"),
                UserHandler(noUserId)
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
    fun `getStatsForTimeSpanInterval aggregates data from contribs with and without hashtags`() {
        val startDate = Instant.parse("2021-01-01T00:00:00Z")  // epoch seconds 1630486233 2021-09-01 08:50:33
        val endDate = Instant.parse("2022-01-01T00:00:00Z")
        val hashtagHandler = HashtagHandler("")
        val interval = "P1Y"
        val result = this.repo.getTopicStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            interval,
            this.emptyListCountryHandler,
            TopicHandler(topic)
        )

        // year 2023 has 3 distinct userids with different and without hashtags
        assertEquals(-2.0, (result["topic_result"] as DoubleArray)[0])
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
    fun `getTopicStatsForTimeSpanCountry returns partial data in time span for start and end date without hashtag aggregated by country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("")
        val result = this.repo.getTopicStatsForTimeSpanCountry(hashtagHandler, startDate, endDate, TopicHandler(topic))

        println(result)
        result.forEachIndexed { counter, it -> println(" $counter $it") }
        assertEquals(2, result.size)
        assertEquals(5, result[0].size)

        assertEquals(2L, result[0]["topic_result"])
        assertEquals("BOL", result[0]["country"])

        assertEquals(1L, result[1]["topic_result"])
        assertEquals("BRA", result[1]["country"])
    }

    @Test
    fun `getTopicStatsForTimeSpanCountry returns partial data in time span for start and end date with hashtag aggregated by country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("hotmicrograntcovid19")
        val result = this.repo.getTopicStatsForTimeSpanCountry(hashtagHandler, startDate, endDate, TopicHandler(topic))

        println(result)
        result.forEachIndexed { counter, it -> println(" $counter $it") }
        assertEquals(1, result.size)
        assertEquals(5, result[0].size)

        assertEquals(-1L, result[0]["topic_result"])
        assertEquals("BRA", result[0]["country"])
    }

    @Test
    fun `getStatsForUserIdForAllHotTMProjects returns stats for only one userid`() {
        val result = this.repo.getTopicStatsForTimeSpan(
            HashtagHandler("hotosm-project-*"),
            null,
            null,
            CountryHandler(emptyList()),
            TopicHandler(topic),
            UserHandler("4362353")
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(-1L, result["topic_result"])
        assertEquals(5, result.size)
    }

    @Test
    fun `getStatsForUserIdForAllHotTMProjects returns zeros for unavailable user id`() {
        val result = this.repo.getTopicStatsForTimeSpan(
            HashtagHandler("hotosm-project-*"),
            null,
            null,
            CountryHandler(emptyList()),
            TopicHandler(topic),
            UserHandler("2381"),
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(0L, result["topic_result"])
    }

    @Test
    fun `getTopicStatsForTimeSpan returns user values for different hashtag`() {
        val result = this.repo.getTopicStatsForTimeSpan(
            HashtagHandler("&uganda"),
            null,
            null,
            CountryHandler(emptyList()),
            TopicHandler("building"),
            UserHandler("6791950"),
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(1L, result["topic_result"])
    }
}


