package org.heigit.ohsome.stats

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
        "roads" to 1326.4405878618195,
        "buildings" to 22,
        "edits" to 22,
        "latest" to "2017-12-19T00:52:03",
        "hashtag" to "&uganda"
    )


    @Test
    @Sql(*["/init_schema.sql", "/stats_400rows.sql"])
    fun `stats should return all data`() {

        val result = this.repo.getStats("&uganda")
        println(result)

        assertEquals(7, result.size)
        assertEquals(expected.toString(), result.toString())

    }

    @Test
    fun `getStatsForTimeSpan should still return all data in time span when using default time span`() {

        val timeSpanResult = this.repo.getStatsForTimeSpan("&ui-state")
        val result = this.repo.getStats("&ui-state")
        println(timeSpanResult)

        assertEquals(result.size, timeSpanResult.size)
        assertEquals(result["changesets"], timeSpanResult["changesets"])
        assertEquals(result["latest"], timeSpanResult["latest"])
    }
    @Test
    fun `getStatsForTimeSpan returns partial data in time span`() {
        val startDate = Instant.ofEpochSecond(1420991470, 0)
        val endDate = Instant.ofEpochSecond(1420992000, 0)

        val result = this.repo.getStatsForTimeSpan("&ui-state", startDate, endDate)
        println(result)

        assertEquals(7, result.size)
        assertEquals("3", result["changesets"].toString())
        assertEquals("2015-01-11T15:59:06", result["latest"].toString())
    }
    @Test
    fun `getStatsForTimeSpan returns partial data in time span for start date only`() {
        val start = Instant.ofEpochSecond(1420991470, 0)

        val result = this.repo.getStatsForTimeSpan("&ui-state", startDate = start)
        println(result)

        assertEquals(7, result.size)
        assertEquals("8", result["changesets"].toString())
        assertEquals("2015-01-11T19:45:10", result["latest"].toString())
    }
    @Test
    fun `getStatsForTimeSpan returns partial data in time span for end date only`() {
        val endDate = Instant.ofEpochSecond(  1420992000, 0)

        val result = this.repo.getStatsForTimeSpan("&ui-state", endDate = endDate)
        println(result)

        assertEquals(7, result.size)
        assertEquals("4", result["changesets"].toString())
        assertEquals("2015-01-11T15:59:06", result["latest"].toString())
    }


}


