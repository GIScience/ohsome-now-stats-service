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
import org.springframework.web.util.UriBuilder
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI


//TODO: extract superclass for system tests?
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@Sql(*[
    "/topics/init_schema_place_view.sql",
    "/topics/init_schema_healthcare_view.sql",
    "/topics/init_schema_amenity_view.sql",
    "/topics/topic_place_40rows.sql",
    "/topics/topic_healthcare_40rows.sql",
    "/topics/topic_amenity_40rows.sql"
])
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


    val topic1 = "place"
    val topic2 = "healthcare"
    val topic3 = "amenity"
    val topics = listOf(topic1, topic2)


    @Test
    @DisplayName("GET /topic/amenity?hashtag=osmliberia")
    fun `get topic amenity`() {

        val url = { uriBuilder: UriBuilder ->
            uriBuilder
                .path("/topic/$topic3")
                .queryParam("hashtag", "osmliberia")
                .build()
        }

        doGetAndAssertThat(url)
            .jsonPath("$.result.$topic3.value").isEqualTo(23)
            .jsonPath("$.result.$topic3.hashtag").isEqualTo("osmliberia")
            .jsonPath("$.result.$topic3.topic").isEqualTo("$topic3")

            .jsonPath("$.query.timespan.startDate").exists()
            .jsonPath("$.query.timespan.endDate").exists()
    }


    @Test
    @DisplayName("GET /topic/place,healthcare?hashtag=hotmicrogrant*")
    fun `get topics place and healthcare`() {

        val url = { uriBuilder: UriBuilder ->
            uriBuilder
                .path("/topic/${topics.joinToString()}")
                .queryParam("hashtag", "hotmicrogrant*")
                .build()
        }

        doGetAndAssertThat(url)
            .jsonPath("$.result.$topic1.value").isEqualTo(5)
            //TODO: check if this is a bug: should be 'hotmicrogrant*' instead of 'hotmicrogrant'
            .jsonPath("$.result.$topic1.hashtag").isEqualTo("hotmicrogrant")
            .jsonPath("$.result.$topic1.topic").isEqualTo("place")
            .jsonPath("$.result.$topic2.value").isEqualTo(2)
            .jsonPath("$.result.$topic2.topic").isEqualTo("healthcare")
            .jsonPath("$.query.timespan.startDate").exists()
            .jsonPath("$.query.timespan.endDate").exists()
    }


    @Test
    @DisplayName("GET /topic/place,healthcare/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M")
    fun `get topics place and healthcare by interval`() {

        val url = { uriBuilder: UriBuilder ->
            uriBuilder
                .path("/topic/${topics.joinToString()}/interval")
                .queryParam("hashtag", "hotmicrogrant*")
                .queryParam("startdate", "2015-01-01T00:00:00Z")
                .queryParam("enddate", "2018-01-01T00:00:00Z")
                .queryParam("interval", "P1M")
                .build()
        }

        doGetAndAssertThat(url)
            .jsonPath("$.result.$topic1[0].value").isEqualTo(3)
            .jsonPath("$.result.$topic1[0].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[0].startDate").isEqualTo("2015-01-01T00:00")
            .jsonPath("$.result.$topic1[0].endDate").isEqualTo("2015-02-01T00:00")
            .jsonPath("$.result.$topic2[0].startDate").isEqualTo("2015-01-01T00:00")
            .jsonPath("$.result.$topic2[0].endDate").isEqualTo("2015-02-01T00:00")

            .jsonPath("$.result.$topic1[35].value").isEqualTo(2)
            .jsonPath("$.result.$topic2[35].value").isEqualTo(0)
            .jsonPath("$.result.$topic1[35].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[35].startDate").isEqualTo("2017-12-01T00:00")
            .jsonPath("$.result.$topic1[35].endDate").isEqualTo("2018-01-01T00:00")
            .jsonPath("$.result.$topic2[35].startDate").isEqualTo("2017-12-01T00:00")
            .jsonPath("$.result.$topic2[35].endDate").isEqualTo("2018-01-01T00:00")

            .jsonPath("$.query.timespan.startDate").exists()
            .jsonPath("$.query.timespan.endDate").exists()
    }


    @Test
    @DisplayName("GET /topic/place,healthcare/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M&countries=BOL")
    fun `get topics place and healthcare by interval for one country`() {

        val url = { uriBuilder: UriBuilder ->
            uriBuilder
                .path("/topic/${topics.joinToString()}/interval")
                .queryParam("hashtag", "hotmicrogrant*")
                .queryParam("startdate", "2015-01-01T00:00:00Z")
                .queryParam("enddate", "2018-01-01T00:00:00Z")
                .queryParam("interval", "P1M")
                .queryParam("countries", "BOL")
                .build()
        }

        doGetAndAssertThat(url)
            .jsonPath("$.result.$topic1[0].value").isEqualTo(0)
            .jsonPath("$.result.$topic1[0].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[0].startDate").isEqualTo("2015-01-01T00:00")
            .jsonPath("$.result.$topic1[0].endDate").isEqualTo("2015-02-01T00:00")
            .jsonPath("$.result.$topic2[0].startDate").isEqualTo("2015-01-01T00:00")
            .jsonPath("$.result.$topic2[0].endDate").isEqualTo("2015-02-01T00:00")

            .jsonPath("$.result.$topic1[35].value").isEqualTo(2)
            .jsonPath("$.result.$topic1[35].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[35].startDate").isEqualTo("2017-12-01T00:00")
            .jsonPath("$.result.$topic1[35].endDate").isEqualTo("2018-01-01T00:00")

            .jsonPath("$.query.timespan.startDate").exists()
            .jsonPath("$.query.timespan.endDate").exists()

    }


    @Test
    @DisplayName("GET /topic/place,healthcare/interval?hashtag=hotmicrogrant*&enddate=2018-01-01T00:00:00Z&interval=P1M")
    fun `get topics place and healthcare by interval for all countries without start date`() {

        val url = { uriBuilder: UriBuilder ->
            uriBuilder
                .path("/topic/${topics.joinToString()}/interval")
                .queryParam("hashtag", "hotmicrogrant*")
                .queryParam("enddate", "2018-01-01T00:00:00Z")
                .queryParam("interval", "P1M")
                .build()
        }

        doGetAndAssertThat(url)
            .jsonPath("$.result.$topic1[0].value").isEqualTo(0)
            .jsonPath("$.result.$topic1[0].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[0].startDate").isEqualTo("1970-01-01T00:00")
            .jsonPath("$.result.$topic1[0].endDate").isEqualTo("1970-02-01T00:00")

            .jsonPath("$.result.$topic1[540].value").isEqualTo(3)
            .jsonPath("$.result.$topic1[540].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[540].startDate").isEqualTo("2015-01-01T00:00")
            .jsonPath("$.result.$topic1[540].endDate").isEqualTo("2015-02-01T00:00")

            .jsonPath("$.result.$topic1[575].value").isEqualTo(2)
            .jsonPath("$.result.$topic1[575].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[575].startDate").isEqualTo("2017-12-01T00:00")
            .jsonPath("$.result.$topic1[575].endDate").isEqualTo("2018-01-01T00:00")

            .jsonPath("$.query.timespan.startDate").exists()
            .jsonPath("$.query.timespan.endDate").exists()

    }


    @Test
    @DisplayName("GET /topic/place,healthcare/country?hashtag=*&startdate=1970-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z")
    fun `get topics place and healthcare by country`() {

        val url = { uriBuilder: UriBuilder ->
            uriBuilder
                .path("/topic/${topics.joinToString()}/country")
                .queryParam("hashtag", "*")
                .queryParam("startdate", "1970-01-01T00:00:00Z")
                .queryParam("enddate", "2024-01-01T00:00:00Z")
                .build()
        }

        doGetAndAssertThat(url)
            .jsonPath("$.result.$topic1[0].value").isEqualTo(2)
            .jsonPath("$.result.$topic1[0].topic").isEqualTo("place")
            .jsonPath("$.result.$topic2[0].topic").isEqualTo("healthcare")
            .jsonPath("$.result.$topic1[0].country").isEqualTo("BOL")
            .jsonPath("$.result.$topic2[0].country").isEqualTo("BEL")

            .jsonPath("$.result.$topic1[1].value").isEqualTo(3)
            .jsonPath("$.result.$topic1[1].topic").isEqualTo("place")
            .jsonPath("$.result.$topic1[1].country").isEqualTo("BRA")
            .jsonPath("$.result.$topic2[1].country").isEqualTo("BRA")

            .jsonPath("$.query.timespan.startDate").exists()
            .jsonPath("$.query.timespan.endDate").exists()

    }


    private fun doGetAndAssertThat(url: (UriBuilder) -> URI) = client()
        .get()
        .uri(url)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()


    private fun client() = WebTestClient
        .bindToServer()
        .baseUrl("http://localhost:$port")
        .build()


}


