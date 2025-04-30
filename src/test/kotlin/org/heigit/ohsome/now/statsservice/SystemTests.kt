package org.heigit.ohsome.now.statsservice

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriBuilder
import org.springframework.web.util.UriComponentsBuilder
import org.testcontainers.junit.jupiter.Container
import java.net.URI
import java.util.Optional
import java.util.stream.Stream


@SpringTestWithClickhouse
@DisplayName("system tests")
class SystemTests {


    @LocalServerPort
    var port: Int = 0


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)

    @Autowired
    lateinit var appProperties: AppProperties


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = createClickhouseContainer()


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("for stats queries")
    @WithStatsData
    inner class StatsTests {

        val normalHashtag = "&uganda"
        val wildcardHashtag = "hotosm-project-*"

        fun hashtagProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(null, 4, 4, 0.0, 0, 3, "2017-12-19T00:52:03Z"),
            Arguments.of("", 4, 4, 0.0, 0, 3, "2017-12-19T00:52:03Z"),
            Arguments.of(normalHashtag, 1, 1, 1.059, 1, 0, "2017-12-19T00:52:03Z"),
            Arguments.of(wildcardHashtag, 1, 1, 0.0, 0, 1, "2016-03-05T14:00:20Z")
        )

        @ParameterizedTest(name = "Test GET /stats with hashtag = {0}")
        @MethodSource("hashtagProvider")
        @DisplayName("GET /stats with different hashtags types")
        fun `get stats for different hashtags`(
            hashtag: String?,
            expectedChangesets: Int,
            expectedUsers: Int,
            expectedRoads: Double,
            expectedBuildings: Int,
            expectedEdits: Int,
            expectedLatest: String
        ) {

            val startDate = "2015-01-01T00:00:00Z"
            val endDate = "2018-01-01T00:00:00Z"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats")
                    .queryParamIfPresent("hashtag", Optional.ofNullable(hashtag))
                    .queryParam("startdate", startDate)
                    .queryParam("enddate", endDate)
                    .build()
            }

            println("Test URL: " + url(UriComponentsBuilder.newInstance()))

            val result = doGetAndAssertThat(url)

            result
                .jsonPath("$.result.changesets").isEqualTo(expectedChangesets)
                .jsonPath("$.result.users").isEqualTo(expectedUsers)
                .jsonPath("$.result.roads").isEqualTo(expectedRoads)
                .jsonPath("$.result.buildings").isEqualTo(expectedBuildings)
                .jsonPath("$.result.edits").isEqualTo(expectedEdits)
                .jsonPath("$.result.latest").isEqualTo(expectedLatest)
                .jsonPath("$.query.timespan.startDate").isEqualTo(startDate)
                .jsonPath("$.query.timespan.endDate").isEqualTo(endDate)

            if (hashtag.isNullOrBlank()) {
                result.jsonPath("$.query.hashtag").isEmpty()
            } else {
                result.jsonPath("$.query.hashtag").isEqualTo(hashtag)
            }
        }


        @Test
        @DisplayName("GET /stats/&uganda")
        fun `get stats for hashtag`() {

            val hashtag = "&uganda"
            val startDate = "2015-01-01T00:00:00Z"
            val endDate = "2018-01-01T00:00:00Z"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/$hashtag")
                    .queryParam("startdate", startDate)
                    .queryParam("enddate", endDate)
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.changesets").isEqualTo(1)
                .jsonPath("$.result.users").isEqualTo(1)
                .jsonPath("$.result.roads").isEqualTo(1.059)
                .jsonPath("$.result.buildings").isEqualTo(1)
                .jsonPath("$.result.edits").isEqualTo(0)
                .jsonPath("$.result.latest").isEqualTo("2017-12-19T00:52:03Z")

                .jsonPath("$.query.timespan.startDate").isEqualTo(startDate)
                .jsonPath("$.query.timespan.endDate").isEqualTo(endDate)
                .jsonPath("$.query.hashtag").isEqualTo(hashtag)
        }


        @Test
        @DisplayName("GET /stats/hashtags/&group,&uganda")
        fun `get stats grouped by 2 hashtags`() {

            val hashtag1 = "&group"
            val hashtag2 = "&uganda"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/hashtags/$hashtag1,$hashtag2")
                    .build()
            }


            doGetAndAssertThat(url)
                .jsonPath("$.result.$hashtag1.changesets").isEqualTo(1)
                .jsonPath("$.result.$hashtag1.users").isEqualTo(1)
                .jsonPath("$.result.$hashtag1.roads").isEqualTo(0)
                .jsonPath("$.result.$hashtag1.buildings").isEqualTo(0)
                .jsonPath("$.result.$hashtag1.edits").isEqualTo(7)
                .jsonPath("$.result.$hashtag1.latest").isEqualTo("2021-12-09T13:01:28Z")

                .jsonPath("$.result.$hashtag2.changesets").isEqualTo(1)
                .jsonPath("$.result.$hashtag2.users").isEqualTo(1)
                .jsonPath("$.result.$hashtag2.roads").isEqualTo(-0.36)
                .jsonPath("$.result.$hashtag2.buildings").isEqualTo(1)
                .jsonPath("$.result.$hashtag2.edits").isEqualTo(0)

                .jsonPath("$.result.$hashtag2.latest").isEqualTo("2017-12-19T00:52:03Z")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /stats/&group/interval?interval=P1Y")
        fun `get stats grouped by time interval`() {

            val hashtag = "&group"
            val interval = "P1Y"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/$hashtag/interval")
                    .queryParam("interval", interval)
                    .build()
            }


            doGetAndAssertThat(url)

                // no results in 1970
                .jsonPath("$.result.changesets[0]").isEqualTo(0)
                .jsonPath("$.result.users[0]").isEqualTo(0)
                .jsonPath("$.result.roads[0]").isEqualTo(0)
                .jsonPath("$.result.buildings[0]").isEqualTo(0)
                .jsonPath("$.result.edits[0]").isEqualTo(0)

                // some results in 2021
                .jsonPath("$.result.changesets[51]").isEqualTo(1)
                .jsonPath("$.result.users[51]").isEqualTo(1)
                .jsonPath("$.result.roads[51]").isEqualTo(0)
                .jsonPath("$.result.buildings[51]").isEqualTo(0)
                .jsonPath("$.result.edits[51]").isEqualTo(7)

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
                .jsonPath("$.query.timespan.interval").isEqualTo(interval)
                .jsonPath("$.query.hashtag").isEqualTo(hashtag)

        }


        @Test
        @DisplayName("GET /stats/&*/country")
        fun `get stats grouped by country`() {

            val hashtag = "&*"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/$hashtag/country")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result[0].changesets").isEqualTo(3)
                .jsonPath("$.result[0].users").isEqualTo(2)
                .jsonPath("$.result[0].roads").isEqualTo(0.0)
                .jsonPath("$.result[0].buildings").isEqualTo(0)
                .jsonPath("$.result[0].edits").isEqualTo(1)

                .jsonPath("$.result[0].latest").isEqualTo("2021-12-09T13:01:28Z")
                .jsonPath("$.result[0].country").isEqualTo("BEL")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
                .jsonPath("$.query.hashtag").isEqualTo(hashtag)
        }

        @Test
        @DisplayName("GET /hashtags")
        fun `get hashtags gets all hashtags`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/hashtags")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result[0].hashtag").isEqualTo("&group")
        }


        @Test
        @DisplayName("GET /hot-tm-user?userid=2186388")
        fun `get userstats with good token`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/hot-tm-user")
                    .queryParam("userId", "2186388")
                    .build()
            }

            val response = client()
                .get()
                .uri(url)
                .header("Authorization", "Basic ${appProperties.token}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()

            response
                .jsonPath("$.result.roads_created_km").isEqualTo(0.0)
                .jsonPath("$.result.roads_modified_longer_km").isEqualTo(0.0)
                .jsonPath("$.result.buildings_added").isEqualTo(0.0)
        }

    }

    @Nested
    @DisplayName("for topic queries")
    @WithTopicData
    inner class TopicTests {

        val topic1 = "place"
        val topic2 = "healthcare"
        val topic3 = "amenity"
        val topic4 = "waterway"
        val topics = listOf(topic1, topic2, topic4)


        @Test
        @DisplayName("GET /topic/kartoffelsupp?hashtag=osmliberia")
        fun `a bad topic time leads to a  BAD_REQUEST (400) error instead of a INTERNAL_SERVER_ERROR (500) error - timeSpan`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/kartoffelsupp")
                    .queryParam("hashtag", "osmliberia")
                    .build()
            }

            assertBadRequestResponse(url)
        }


        @Test
        @DisplayName("GET /topic/kartoffelsupp/interval?hashtag=osmliberia&interval=P1M")
        fun `a bad topic time leads to a  BAD_REQUEST (400) error instead of a INTERNAL_SERVER_ERROR (500) error - by interval`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/kartoffelsupp/interval")
                    .queryParam("hashtag", "osmliberia")
                    .queryParam("interval", "P1M")
                    .build()
            }

            assertBadRequestResponse(url)
        }


        @Test
        @DisplayName("GET /topic/kartoffelsupp/country?hashtag=osmliberia")
        fun `a bad topic time leads to a  BAD_REQUEST (400) error instead of a INTERNAL_SERVER_ERROR (500) error - by country`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/kartoffelsupp/country")
                    .queryParam("hashtag", "osmliberia")
                    .build()
            }

            assertBadRequestResponse(url)
        }


        @Test
        @DisplayName("GET /topic/amenity?hashtag=osmliberia")
        fun `get topic amenity`() {

            val hashtag = "hotosm-project-osmliberia"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/$topic3")
                    .queryParam("hashtag", hashtag)
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.$topic3.value").isEqualTo(23)
                .jsonPath("$.result.$topic3.topic").isEqualTo("$topic3")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /topic/place,healthcare,waterway?hashtag=hotmicrogrant*")
        fun `get topics place and healthcare and waterway`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/${topics.joinToString(separator = ",")}")
                    .queryParam("hashtag", "hotmicrogrant*")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.$topic1.value").isEqualTo(5)
                //TODO: check if this is a bug: should be 'hotmicrogrant*' instead of 'hotmicrogrant'
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic2.topic").isEqualTo("healthcare")
                .jsonPath("$.result.$topic2.value").isEqualTo(2)
                .jsonPath("$.result.$topic4.topic").isEqualTo("waterway")
                .jsonPath("$.result.$topic4.value").isEqualTo(2.207)
                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /topic/place,healthcare/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M")
        fun `get topics place and healthcare by interval`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/${topics.joinToString(separator = ",")}/interval")
                    .queryParam("hashtag", "hotmicrogrant*")
                    .queryParam("startdate", "2015-01-01T00:00:00Z")
                    .queryParam("enddate", "2018-01-01T00:00:00Z")
                    .queryParam("interval", "P1M")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.$topic1.value[0]").isEqualTo(3)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[0]").isEqualTo("2015-01-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[0]").isEqualTo("2015-02-01T00:00:00")
                .jsonPath("$.result.$topic2.startDate[0]").isEqualTo("2015-01-01T00:00:00")
                .jsonPath("$.result.$topic2.endDate[0]").isEqualTo("2015-02-01T00:00:00")

                .jsonPath("$.result.$topic1.value[35]").isEqualTo(2)
                .jsonPath("$.result.$topic2.value[35]").isEqualTo(0)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[35]").isEqualTo("2017-12-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[35]").isEqualTo("2018-01-01T00:00:00")
                .jsonPath("$.result.$topic2.startDate[35]").isEqualTo("2017-12-01T00:00:00")
                .jsonPath("$.result.$topic2.endDate[35]").isEqualTo("2018-01-01T00:00:00")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /topic/place,healthcare/interval?hashtag=hotmicrogrant*&startdate=2015-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z&interval=P1M&countries=BOL")
        fun `get topics place and healthcare by interval for one country`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/${topics.joinToString(separator = ",")}/interval")
                    .queryParam("hashtag", "hotmicrogrant*")
                    .queryParam("startdate", "2015-01-01T00:00:00Z")
                    .queryParam("enddate", "2018-01-01T00:00:00Z")
                    .queryParam("interval", "P1M")
                    .queryParam("countries", "BOL")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.$topic1.value[0]").isEqualTo(0)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[0]").isEqualTo("2015-01-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[0]").isEqualTo("2015-02-01T00:00:00")
                .jsonPath("$.result.$topic2.startDate[0]").isEqualTo("2015-01-01T00:00:00")
                .jsonPath("$.result.$topic2.endDate[0]").isEqualTo("2015-02-01T00:00:00")

                .jsonPath("$.result.$topic1.value[35]").isEqualTo(2)
                .jsonPath("$.result.$topic1.modified.unit_more").doesNotExist()
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[35]").isEqualTo("2017-12-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[35]").isEqualTo("2018-01-01T00:00:00")

                .jsonPath("$.result.$topic4.value[35]").isEqualTo(0)
                .jsonPath("$.result.$topic4.modified.unit_more[35]").isEqualTo(0.0)
                .jsonPath("$.result.$topic4.topic").isEqualTo("waterway")
                .jsonPath("$.result.$topic4.startDate[35]").isEqualTo("2017-12-01T00:00:00")
                .jsonPath("$.result.$topic4.endDate[35]").isEqualTo("2018-01-01T00:00:00")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /topic/place,healthcare/interval?hashtag=hotmicrogrant*&enddate=2018-01-01T00:00:00Z&interval=P1M")
        fun `get topics place and healthcare by interval for all countries without start date`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/${topics.joinToString(separator = ",")}/interval")
                    .queryParam("hashtag", "hotmicrogrant*")
                    .queryParam("enddate", "2018-01-01T00:00:00Z")
                    .queryParam("interval", "P1Y")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.$topic1.value[0]").isEqualTo(0)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[0]").isEqualTo("1970-01-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[0]").isEqualTo("1971-01-01T00:00:00")

                .jsonPath("$.result.$topic1.value[45]").isEqualTo(3)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[45]").isEqualTo("2015-01-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[45]").isEqualTo("2016-01-01T00:00:00")

                .jsonPath("$.result.$topic1.value[47]").isEqualTo(2)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic1.startDate[47]").isEqualTo("2017-01-01T00:00:00")
                .jsonPath("$.result.$topic1.endDate[47]").isEqualTo("2018-01-01T00:00:00")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /topic/place,healthcare/country?hashtag=*&startdate=1970-01-01T00:00:00Z&enddate=2018-01-01T00:00:00Z")
        fun `get topics place and healthcare by country`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/${topics.joinToString(separator = ",")}/country")
                    .queryParam("hashtag", "h*")
                    .queryParam("startdate", "1970-01-01T00:00:00Z")
                    .queryParam("enddate", "2024-01-01T00:00:00Z")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.$topic1[0].value").isEqualTo(2)
                .jsonPath("$.result.$topic1[0].topic").isEqualTo("place")
                .jsonPath("$.result.$topic2[0].topic").isEqualTo("healthcare")
                .jsonPath("$.result.$topic1[0].country").isEqualTo("BOL")
                .jsonPath("$.result.$topic2[0].country").isEqualTo("BRA")

                .jsonPath("$.result.$topic1[1].value").isEqualTo(2)
                .jsonPath("$.result.$topic1[1].topic").isEqualTo("place")
                .jsonPath("$.result.$topic1[1].country").isEqualTo("BRA")
                .jsonPath("$.result.$topic2[1].country").isEqualTo("FRA")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /topic/place,healthcare/user?userid=4362353")
        fun `get userstats topics with good token`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/${topics.joinToString(separator = ",")}/user")
                    .queryParam("userId", "4362353")
                    .build()
            }

            val response = client()
                .get()
                .uri(url)
                .header("Authorization", "Basic ${appProperties.token}")
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()

            response
                .jsonPath("$.result.$topic1.value").isEqualTo(-1)
                .jsonPath("$.result.$topic1.topic").isEqualTo("place")
                .jsonPath("$.result.$topic2.value").isEqualTo(0)
                .jsonPath("$.result.$topic2.value").isEqualTo(0)
                .jsonPath("$.result.$topic2.modified.count_modified").isEqualTo(0)
                .jsonPath("$.result.$topic2.topic").isEqualTo("healthcare")
        }
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

    private fun assertBadRequestResponse(url: (UriBuilder) -> URI) {
        client()
            .get()
            .uri(url)
            .exchange()
            .expectStatus()
            .isBadRequest
    }

}




