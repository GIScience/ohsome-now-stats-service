package org.heigit.ohsome.stats

import org.heigit.ohsome.stats.utils.HashtagHandler
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
class StatsRepoIntegrationTests {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = ClickHouseContainer("clickhouse/clickhouse-server")


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) = registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }

    @Autowired
    lateinit var repo: StatsRepo

    val expected = mapOf(
            "changesets" to 1,
            "users" to 1,
            "roads" to 140,
            "buildings" to 1,
            "edits" to 1,
            "latest" to "2017-12-19T00:52:03",
            "hashtag" to "&uganda"
    )

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan should return all data when using no time span`() {
        val hashtagHandler = HashtagHandler("&uganda")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null)
        assertEquals(7, result.size)
        println(result)
        assertEquals(expected.toString(), result.toString())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns partial data in time span`() {
        val startDate = Instant.ofEpochSecond(1457186410)
        val endDate = Instant.ofEpochSecond(1457186430)
        println(startDate)
        val hashtagHandler = HashtagHandler("&")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, startDate, endDate)
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
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, startDate, null)
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
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, endDate)
        println(result)

        assertEquals(7, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2021-12-09T13:01:28", result["latest"].toString())
    }

    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `getStatsForTimeSpan returns combined data of multiple hashtags with wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group*")
        val resultWildCard = this.repo.getStatsForTimeSpan(hashtagHandlerWildcard, null, null)

        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null)

        assertTrue(result["changesets"].toString().toInt() < resultWildCard["changesets"].toString().toInt())
    }


}


