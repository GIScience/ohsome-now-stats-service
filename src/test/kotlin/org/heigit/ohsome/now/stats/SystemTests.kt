package org.heigit.ohsome.now.stats

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers



//TODO: parameter explizit angeben statt in der URL?

//TODO: extract superclass for system tests?
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class SystemTests {


    @LocalServerPort
    var port: Int = 0


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


    val topic = "place"


    @Test
    @DisplayName("GET /topic/topic?hashtag=hotmicrogrant*")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic place`() {

        client()
            .get()
            .uri("/topic/$topic?hashtag=hotmicrogrant*")
            .exchange()
            .expectStatus()
                .isOk
            .expectBody()
                .jsonPath("$.result.value").isEqualTo(5)

                //TODO: check if this is a bug: should be 'hotmicrogrant*' instead of 'hotmicrogrant'
                .jsonPath("$.result.hashtag").isEqualTo("hotmicrogrant")
                .jsonPath("$.result.topic").isEqualTo("place")
                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
    }

    private fun client() = WebTestClient
        .bindToServer()
        .baseUrl("http://localhost:$port")
        .build()


    @Test
    @DisplayName("GET /topic/place/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic by interval`() {

        client()
            .get()
            .uri("topic/$topic/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M")
            .exchange()
            .expectStatus()
                .isOk
            .expectBody()
                .jsonPath("$.result[0].value").isEqualTo(3)
                .jsonPath("$.result[0].topic").isEqualTo("place")
                .jsonPath("$.result[0].startDate").isEqualTo("2015-01-01T00:00")
                .jsonPath("$.result[0].endDate").isEqualTo("2015-02-01T00:00")

                .jsonPath("$.result[35].value").isEqualTo(2)
                .jsonPath("$.result[35].topic").isEqualTo("place")
                .jsonPath("$.result[35].startDate").isEqualTo("2017-12-01T00:00")
                .jsonPath("$.result[35].endDate").isEqualTo("2018-01-01T00:00")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
    }




    @Test
    @DisplayName("GET topic/place/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M&countries=BOL")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic by interval for one country`() {

        client()
            .get()
            .uri("topic/$topic/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z" +
                    "&interval=P1M&countries=BOL")
            .exchange()
            .expectStatus()
                .isOk
            .expectBody()
                .jsonPath("$.result[0].value").isEqualTo(0)
                .jsonPath("$.result[0].topic").isEqualTo("place")
                .jsonPath("$.result[0].startDate").isEqualTo("2015-01-01T00:00")
                .jsonPath("$.result[0].endDate").isEqualTo("2015-02-01T00:00")

                .jsonPath("$.result[35].value").isEqualTo(2)
                .jsonPath("$.result[35].topic").isEqualTo("place")
                .jsonPath("$.result[35].startDate").isEqualTo("2017-12-01T00:00")
                .jsonPath("$.result[35].endDate").isEqualTo("2018-01-01T00:00")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()

    }




    /*


    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
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
            topic
        )

        println(result)
        assertEquals(84, result.size)
        assertEquals(3, result[0].size)
        assertEquals("2015-01-01T00:00", result[0]["startdate"].toString())

        // 3 new places in 'BRA' at the beginning of the interval but contries are restricted to 'BOL'
        assertEquals("0", result[0]["topic_result"].toString())
        assertEquals("2", result[35]["topic_result"].toString())
    }


    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
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
            topic
        )

        println(result)
        assertEquals(624, result.size)
        assertEquals(3, result[0].size)
        assertEquals("1970-01-01T00:00", result[0]["startdate"].toString())

        assertEquals("3", result[540]["topic_result"].toString())
        assertEquals("2", result[575]["topic_result"].toString())
    }


    @Test
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
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
            topic
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
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `getTopicStatsForTimeSpanCountry returns partial data in time span for start and end date with hashtag aggregated by month and country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getTopicStatsForTimeSpanCountry(hashtagHandler, startDate, endDate, "place")

        println(result)
        result.forEachIndexed { counter, it -> println(" $counter $it") }
        assertEquals(2, result.size)
        assertEquals(2, result[0].size)

        assertEquals(2L, result[0]["topic_result"])
        assertEquals("BOL", result[0]["country"])

        assertEquals(3L, result[1]["topic_result"])
        assertEquals("BRA", result[1]["country"])
    }
*/


}


