package org.heigit.ohsome.now.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
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
import java.time.LocalDateTime


@SpringBootTest(webEnvironment = NONE)
@Testcontainers
class StatsRepoIntegrationTests {


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
    lateinit var repo: StatsRepo

    val expected = mapOf(
        "changesets" to 1,
        "users" to 1,
        "roads" to -0.009,
        "buildings" to 0,
        "edits" to 0,
        "latest" to "2017-12-19T00:52:03",
        "hashtag" to "&uganda"
    )

    private val emptyListCountryHandler = CountryHandler(emptyList())


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan should return all data when using no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("&uganda")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler)
        assertEquals(7, result.size)
        println(result)
        assertEquals(expected.toString(), result.toString())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan should return all data when using no time span and list of 2 countries`() {
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, CountryHandler(listOf("HUN", "BEL")))
        assertEquals(7, result.size)
        println(result)
        assertEquals(
            "{changesets=4, users=2, roads=0.72, buildings=0, edits=9, latest=2021-12-09T13:01:28, hashtag=}",
            result.toString()
        )
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns partial data in time span`() {
        val startDate = Instant.ofEpochSecond(1457186410)
        val endDate = Instant.ofEpochSecond(1457186430)
        println(startDate)
        val hashtagHandler = HashtagHandler("&")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, startDate, endDate, emptyListCountryHandler)
        println(result)

        assertEquals(7, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2016-03-05T14:00:20", result["latest"].toString())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns partial data in time span for start date only`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, startDate, null, emptyListCountryHandler)
        println(result)

        assertEquals(7, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2021-12-09T13:01:28", result["latest"].toString())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns partial data in time span for end date only`() {
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, endDate, emptyListCountryHandler)
        println(result)

        assertEquals(7, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2021-12-09T13:01:28", result["latest"].toString())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns combined data of multiple hashtags with wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group*")
        val resultWildCard =
            this.repo.getStatsForTimeSpan(hashtagHandlerWildcard, null, null, emptyListCountryHandler)

        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler)

        assertTrue(result["changesets"].toString().toInt() < resultWildCard["changesets"].toString().toInt())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpanAggregate returns disaggregated data with wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group*")
        val resultsWildCard = this.repo.getStatsForTimeSpanAggregate(hashtagHandlerWildcard, null, null)

        assertEquals(2, resultsWildCard.size)

        val hashtags = mutableListOf<String>()
        resultsWildCard.onEach { hashtags.add(it["hashtag"].toString()) }
        assertIterableEquals(listOf("&groupExtra", "&group").sorted(), hashtags.sorted())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpanAggregate returns data in list without wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group")
        val resultsWildCard = this.repo.getStatsForTimeSpanAggregate(hashtagHandlerWildcard, null, null)

        assertEquals(1, resultsWildCard.size)

        val hashtags = mutableListOf<String>()
        resultsWildCard.onEach { hashtags.add(it["hashtag"].toString()) }
        assertIterableEquals(listOf("&group").sorted(), hashtags.sorted())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns all data on everything with only wildcard character`() {
        // wasn't sure about the sql behavior of startWith(""), but it seems that it selects everything like expected
        val hashtagHandlerWildcard = HashtagHandler("*")
        val resultWildCard = this.repo.getStatsForTimeSpan(hashtagHandlerWildcard, null, null, emptyListCountryHandler)

        assertEquals(7, resultWildCard["changesets"].toString().toInt())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpanInterval returns partial data in time span for start and end date with hashtag aggregated by month without countries`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler)
        println(result)
        assertEquals(84, result.size)
        assertEquals(7, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())
        assertEquals("1", result[83]["users"].toString())
        assertEquals("7", result[83]["edits"].toString())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpanInterval returns partial data in time span for start and end date with hashtag aggregated by month with 1 country`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&uganda")
        val result = this.repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", CountryHandler(listOf("XYZ")))
        println(result)
        assertEquals(83, result.size)
        assertEquals(7, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())
        assertEquals("1", result[35]["users"].toString())
        assertEquals("1", result[35]["changesets"].toString())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpanInterval fills data between two dates with zeros`() {
        val startDate = Instant.ofEpochSecond(1503644723)
        val endDate = Instant.ofEpochSecond(1640486233)
        val hashtagHandler = HashtagHandler("&gid")
        val result = this.repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler)
        println(result)
        assertEquals(52, result.size)
        assertEquals(7, result[0].size)
        assertEquals("2017-08-01T00:00", result[0]["startdate"].toString())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpanInterval returns all data when nothing is supplied as startdate`() {
        val startDate = null
        val endDate = Instant.ofEpochSecond(1639054888)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpanInterval(hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler)
        println(result)
        assertEquals(623, result.size)
        assertEquals(7, result[0].size)
        assertEquals("1970-01-01T00:00", result[0]["startdate"].toString())
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsTimeCountry returns partial data in time span for start and  end date with hashtag aggregated by month and country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsForTimeSpanCountry(hashtagHandler, startDate, endDate)
        println(result)
        assertTrue(result is List)
        assertEquals(7, result[0].size)
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForUserIdForAllHotTMProjects returns stats for only one userid`() {
        val result = this.repo.getStatsForUserIdForAllHotTMProjects("2186388")
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(5, result.size)
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForUserIdForAllHotTMProjects returns zeros for unavailable user id`() {
        val result = this.repo.getStatsForUserIdForAllHotTMProjects("2186381")
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(2186381, result["user_id"])
        assertEquals(UnsignedLong.valueOf(0), result["edits"])
        assertEquals(0L, result["buildings"])
        assertEquals(0.0, result["roads"])
        assertEquals(UnsignedLong.valueOf(0), result["changesets"])
    }


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getMostUsedHashtags returns the most used hashtags in given time range for start and enddate`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val result = this.repo.getMostUsedHashtags(startDate, endDate, 10)
        println(result)
        assertTrue(result[0] is Map)
        assertTrue(result[0].containsKey("hashtag"))
        assertTrue(result[0].containsKey("number_of_users"))
        assertTrue(result[0].size == 2)
        assertTrue(result.size == 7)
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getMostUsedHashtags returns empty list when out of time bounds`() {
        val startDate = Instant.ofEpochSecond(1020991470)
        val endDate = Instant.ofEpochSecond(1019054890)
        val result = this.repo.getMostUsedHashtags(startDate, endDate, 10)
        println(result)
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getMetadata returns the minimum and maximum timestamp`() {
        val result = this.repo.getMetadata()
        println(result)
        assertEquals(LocalDateTime.parse("2009-04-22T22:00"), result["min_timestamp"])
    }
}


