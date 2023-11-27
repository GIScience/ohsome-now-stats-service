package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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


    val expected = mapOf(
        "topic_result" to 2,
        "hashtag" to "hotmicrogrant"
    )


    private val emptyListCountryHandler = CountryHandler(emptyList())


    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpan should return all data when using no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler, "place")
        assertEquals(2, result.size)
        println(result)
        assertEquals(expected.toString(), result.toString())
    }


    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanInterval returns partial topic data in time span for start and end date with hashtag aggregated by month without countries`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1640054890)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler)

//        println(result)

        result.forEach(::println)


        assertEquals(83, result.size)
        assertEquals(3, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())
        assertEquals("2", result[35]["topic_result"].toString())
    }




    @Disabled
//    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanInterval returns partial topic data in time span for start and end date with hashtag aggregated by month with 1 country`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1640054890)
        val hashtagHandler = HashtagHandler("hotmicrogrant*")
        val result = this.repo.getTopicStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", CountryHandler(listOf("XYZ")))
        println(result)
        assertEquals(83, result.size)
        assertEquals(7, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())
        assertEquals("1", result[35]["users"].toString())
        assertEquals("1", result[35]["changesets"].toString())
    }


    @Disabled
//    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanInterval fills data between two dates with zeros`() {
        val startDate = Instant.ofEpochSecond(1503644723)
        val endDate = Instant.ofEpochSecond(1640486233)
        val hashtagHandler = HashtagHandler("&gid")
        val result = this.repo.getTopicStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler)
        println(result)
        assertEquals(52, result.size)
        assertEquals(7, result[0].size)
        assertEquals("2017-08-01T00:00", result[0]["startdate"].toString())
    }


    @Disabled
//    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanInterval returns all data when nothing is supplied as startdate`() {
        val startDate = null
        val endDate = Instant.ofEpochSecond(1639054888)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getTopicStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler)
        println(result)
        assertEquals(623, result.size)
        assertEquals(7, result[0].size)
        assertEquals("1970-01-01T00:00", result[0]["startdate"].toString())
    }



}


