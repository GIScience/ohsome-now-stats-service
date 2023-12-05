package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.TopicHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant


@SpringBootTest(webEnvironment = NONE)
@Testcontainers
class TopicRepoIntegrationTests {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = ClickHouseContainer("clickhouse/clickhouse-server")


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
        "hashtag" to "hotmicrogrant"
    )


    private val emptyListCountryHandler = CountryHandler(emptyList())


    @Test
    @Sql(*["/topics/init_schema_place_view.sql", "/topics/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpan should return all data when using no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result =
            this.repo.getTopicStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler, TopicHandler(topic))

        println(result)
        assertEquals(2, result.size)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    @Sql(*["/topics/init_schema_healthcare_view.sql", "/topics/topic_healthcare_40rows.sql"])
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
        assertEquals(2, result.size)
        assertEquals("-3", result["topic_result"].toString())
    }

    @Test
    @Sql(*["/topics/init_schema_place_view.sql", "/topics/topic_place_40rows.sql"])
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
        assertEquals(84, result.size)
        assertEquals(3, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())

        // 3 new places at the beginning of the interval
        assertEquals("3", result[0]["topic_result"].toString())
        assertEquals("2", result[35]["topic_result"].toString())
    }


    @Test
    @Sql(*["/topics/init_schema_place_view.sql", "/topics/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanInterval returns partial topic data in time span for start and end date with hashtag aggregated by month with 1 country`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1640054890)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            CountryHandler(listOf("BOL")),
            TopicHandler(topic)
        )

        println(result)
        assertEquals(84, result.size)
        assertEquals(3, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())

        // 3 new places in 'BRA' at the beginning of the interval but countries are restricted to 'BOL'
        assertEquals("0", result[0]["topic_result"].toString())
        assertEquals("2", result[35]["topic_result"].toString())
    }


    @Test
    @Sql(*["/topics/init_schema_place_view.sql", "/topics/topic_place_40rows.sql"])
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
        assertEquals(624, result.size)
        assertEquals(3, result[0].size)
        assertEquals("1970-01-01T00:00", result[0]["startdate"].toString())

        assertEquals("3", result[540]["topic_result"].toString())
        assertEquals("2", result[575]["topic_result"].toString())
    }


    @Test
    @Sql(*["/topics/init_schema_place_view.sql", "/topics/topic_place_40rows.sql"])
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

        println(result)
        result.forEachIndexed { counter, it -> println(" $counter $it") }

        assertEquals(53, result.size)
        assertEquals(3, result[0].size)

        result.forEach() {
            assertNotNull(it["topic_result"])
        }

        assertEquals("2017-08-01T00:00", result[0]["startdate"].toString())
    }


    @Test
    @Sql(*["/topics/init_schema_place_view.sql", "/topics/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanCountry returns partial data in time span for start and end date with hashtag aggregated by month and country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getTopicStatsForTimeSpanCountry(hashtagHandler, startDate, endDate, TopicHandler(topic))

        println(result)
        result.forEachIndexed { counter, it -> println(" $counter $it") }
        assertEquals(2, result.size)
        assertEquals(2, result[0].size)

        assertEquals(2L, result[0]["topic_result"])
        assertEquals("BOL", result[0]["country"])

        assertEquals(3L, result[1]["topic_result"])
        assertEquals("BRA", result[1]["country"])
    }


}


