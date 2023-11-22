package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        val result = this.repo.getTopicStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler)
        assertEquals(7, result.size)
        println(result)
        assertEquals(expected.toString(), result.toString())
    }


}


