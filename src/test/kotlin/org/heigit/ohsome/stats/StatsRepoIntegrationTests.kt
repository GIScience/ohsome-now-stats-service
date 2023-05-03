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

        val result = this.repo.getStatsForTimeSpan("&uganda")
        println(result)

        assertEquals(7, result.size)
        assertEquals(expected.toString(), result.toString())

    }


}


