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

        val url = { uriBuilder: UriBuilder -> uriBuilder
            .path("/topic/$topic")
            .queryParam("hashtag", "hotmicrogrant*")
            .build()
        }

        client()
            .get()
            .uri(url)
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


    @Test
    @DisplayName("GET /topic/place/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic by interval`() {

        val url = { uriBuilder: UriBuilder -> uriBuilder
            .path("/topic/$topic/interval")
            .queryParam("hashtag", "hotmicrogrant*")
            .queryParam("startdate", "2015-01-01T00:00:00Z")
            .queryParam("enddate", "2018-01-01T00:00:00Z")
            .queryParam("interval", "P1M")
            .build()
        }

        client()
            .get()
            .uri(url)
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
    @DisplayName("GET /topic/place/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M&countries=BOL")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic by interval for one country`() {

        val url = { uriBuilder: UriBuilder -> uriBuilder
            .path("/topic/$topic/interval")
            .queryParam("hashtag", "hotmicrogrant*")
            .queryParam("startdate", "2015-01-01T00:00:00Z")
            .queryParam("enddate", "2018-01-01T00:00:00Z")
            .queryParam("interval", "P1M")
            .queryParam("countries", "BOL")
            .build()
        }

        client()
            .get()
            .uri(url)
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


    @Test
    @DisplayName("GET /topic/place/interval?hashtag=hotmicrogrant*&enddate=2018-01-01T00:00:00Z&interval=P1M")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic by interval for all countries without start date`() {

        val url = { uriBuilder: UriBuilder -> uriBuilder
            .path("/topic/$topic/interval")
            .queryParam("hashtag", "hotmicrogrant*")
            .queryParam("enddate", "2018-01-01T00:00:00Z")
            .queryParam("interval", "P1M")
            .build()
        }

        client()
            .get()
            .uri(url)
            .exchange()
            .expectStatus()
                .isOk
            .expectBody()
                .jsonPath("$.result[0].value").isEqualTo(0)
                .jsonPath("$.result[0].topic").isEqualTo("place")
                .jsonPath("$.result[0].startDate").isEqualTo("1970-01-01T00:00")
                .jsonPath("$.result[0].endDate").isEqualTo("1970-02-01T00:00")

                .jsonPath("$.result[540].value").isEqualTo(3)
                .jsonPath("$.result[540].topic").isEqualTo("place")
                .jsonPath("$.result[540].startDate").isEqualTo("2015-01-01T00:00")
                .jsonPath("$.result[540].endDate").isEqualTo("2015-02-01T00:00")

                .jsonPath("$.result[575].value").isEqualTo(2)
                .jsonPath("$.result[575].topic").isEqualTo("place")
                .jsonPath("$.result[575].startDate").isEqualTo("2017-12-01T00:00")
                .jsonPath("$.result[575].endDate").isEqualTo("2018-01-01T00:00")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()

    }

    @Test
    @DisplayName("GET /topic/place/country?hashtag=*&startdate=1970-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z")
    @Sql(*["/init_schema_place_view.sql", "/topic_place_40rows.sql"])
    fun `get topic by country`() {

        val url = { uriBuilder: UriBuilder -> uriBuilder
            .path("/topic/$topic/country")
            .queryParam("hashtag", "*")
            .queryParam("startdate", "1970-01-01T00:00:00Z")
            .queryParam("enddate", "2018-01-01T00:00:00Z")
            .build()
        }

        client()
            .get()
            .uri(url)
            .exchange()
            .expectStatus()
                .isOk
            .expectBody()
                .jsonPath("$.result[0].value").isEqualTo(2)
                .jsonPath("$.result[0].topic").isEqualTo("place")
                .jsonPath("$.result[0].country").isEqualTo("BOL")

                .jsonPath("$.result[1].value").isEqualTo(3)
                .jsonPath("$.result[1].topic").isEqualTo("place")
                .jsonPath("$.result[1].country").isEqualTo("BRA")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()

    }


    private fun client() = WebTestClient
        .bindToServer()
        .baseUrl("http://localhost:$port")
        .build()


}


