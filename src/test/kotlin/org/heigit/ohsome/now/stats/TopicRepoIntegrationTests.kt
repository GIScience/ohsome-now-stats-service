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


}


