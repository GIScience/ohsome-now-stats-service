package org.heigit.ohsome.now.statsservice

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
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
import java.util.*
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
            Arguments.of(null, 4, 4, 1.983, 2, 3, "2017-12-19T00:52:03Z"),
            Arguments.of("", 4, 4, 1.983, 2, 3, "2017-12-19T00:52:03Z"),
            Arguments.of(normalHashtag, 1, 1, 1.059, 2, 0, "2017-12-19T00:52:03Z"),
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
        ) {

            val startDate = "2015-01-01T00:00:00Z"
            val endDate = "2018-01-01T00:00:00Z"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats")
                    .queryParamIfPresent("hashtag", Optional.ofNullable(hashtag))
                    .queryParam("startdate", startDate)
                    .queryParam("enddate", endDate)
                    .queryParam("topics", listOf("changeset", "edit", "contributor", "building", "road"))
                    .build()
            }

            println("Test URL: " + url(UriComponentsBuilder.newInstance()))

            val result = doGetAndAssertThat(url)

            result
                .jsonPath("$.result.topics.changeset.value").isEqualTo(expectedChangesets)
                .jsonPath("$.result.topics.contributor.value").isEqualTo(expectedUsers)
                .jsonPath("$.result.topics.road.value").isEqualTo(expectedRoads)
                .jsonPath("$.result.topics.building.value").isEqualTo(expectedBuildings)
                .jsonPath("$.result.topics.edit.value").isEqualTo(expectedEdits)
                .jsonPath("$.query.timespan.startDate").isEqualTo(startDate)
                .jsonPath("$.query.timespan.endDate").isEqualTo(endDate)

            if (hashtag.isNullOrBlank()) {
                result.jsonPath("$.query.hashtag").doesNotExist()
            } else {
                result.jsonPath("$.query.hashtag").isEqualTo(hashtag)
            }
        }


        @Test
        @DisplayName("GET /stats?hashtag=&uganda")
        fun `get stats for hashtag`() {

            val hashtag = "&uganda"
            val startDate = "2015-01-01T00:00:00Z"
            val endDate = "2018-01-01T00:00:00Z"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats")
                    .queryParam("hashtag", hashtag)
                    .queryParam("topics", "changeset,edit,contributor,building,road")
                    .queryParam("startdate", startDate)
                    .queryParam("enddate", endDate)
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.topics.changeset.value").isEqualTo(1)
                .jsonPath("$.result.topics.contributor.value").isEqualTo(1)
                .jsonPath("$.result.topics.road.value").isEqualTo(1.059)
                .jsonPath("$.result.topics.building.value").isEqualTo(2)
                .jsonPath("$.result.topics.edit.value").isEqualTo(0)

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
                .jsonPath("$.result.$hashtag2.buildings").isEqualTo(2)
                .jsonPath("$.result.$hashtag2.edits").isEqualTo(0)

                .jsonPath("$.result.$hashtag2.latest").isEqualTo("2017-12-19T00:52:03Z")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }

        @Test
        @DisplayName("GET /stats/hashtags/&*")
        fun `get stats grouped by multiple hashtags with wildcard operator`() {
            val hashtag1 = "&group"
            val hashtag2 = "&uganda"


            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/hashtags/&*")
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
                .jsonPath("$.result.$hashtag2.buildings").isEqualTo(2)
                .jsonPath("$.result.$hashtag2.edits").isEqualTo(0)

                .jsonPath("$.result.$hashtag2.latest").isEqualTo("2017-12-19T00:52:03Z")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
        }


        @Test
        @DisplayName("GET /stats/interval?hashtag=&group&interval=P1Y")
        fun `get stats grouped by time interval`() {

            val hashtag = "&group"
            val interval = "P1Y"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/interval")
                    .queryParam("hashtag", hashtag)
                    .queryParam("interval", interval)
                    .queryParam("topics", "changeset,edit,contributor,building,road")
                    .build()
            }


            doGetAndAssertThat(url)

                // no results in 1970
                .jsonPath("$.result.topics.changeset.value[0]").isEqualTo(0)
                .jsonPath("$.result.topics.contributor.value[0]").isEqualTo(0)
                .jsonPath("$.result.topics.road.value[0]").isEqualTo(0)
                .jsonPath("$.result.topics.building.value[0]").isEqualTo(0)
                .jsonPath("$.result.topics.edit.value[0]").isEqualTo(0)

                // some results in 2021
                .jsonPath("$.result.topics.changeset.value[51]").isEqualTo(1)
                .jsonPath("$.result.topics.contributor.value[51]").isEqualTo(1)
                .jsonPath("$.result.topics.road.value[51]").isEqualTo(0)
                .jsonPath("$.result.topics.building.value[51]").isEqualTo(0)
                .jsonPath("$.result.topics.edit.value[51]").isEqualTo(7)

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
                .jsonPath("$.query.timespan.interval").isEqualTo(interval)
                .jsonPath("$.query.hashtag").isEqualTo(hashtag)

        }

        @Test
        @DisplayName("GET /stats/country?hashtag=&*")
        fun `get stats grouped by country`() {

            val hashtag = "&*"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/country")
                    .queryParam("hashtag", hashtag)
                    .queryParam("topics", "changeset,edit,contributor,building,road")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.topics.changeset[0].value").isEqualTo(3)
                .jsonPath("$.result.topics.contributor[0].value").isEqualTo(2)
                .jsonPath("$.result.topics.road[0].value").isEqualTo(-0.36)
                .jsonPath("$.result.topics.building[0].value").isEqualTo(0)
                .jsonPath("$.result.topics.edit[0].value").isEqualTo(1)
                .jsonPath("$.result.topics.changeset[0].country").isEqualTo("BEL")

                .jsonPath("$.query.timespan.startDate").exists()
                .jsonPath("$.query.timespan.endDate").exists()
                .jsonPath("$.query.hashtag").isEqualTo(hashtag)
        }

        @Test
        @DisplayName("GET /stats with statsTopics and topics")
        fun `get stats with statsTopics and topics`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats")
                    .queryParam("topics", "edit,building")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.topics.building").exists()
                .jsonPath("$.result.topics.edit").exists()
                .jsonPath("$.result.topics.edit.value").exists()
        }

        @Test
        @DisplayName("GET /stats/interval with statsTopics and topics")
        fun `get stats grouped by interval with statsTopics and topics`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/interval")
                    .queryParam("topics", "edit,building")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.topics.building").exists()
                .jsonPath("$.result.topics.edit").exists()
                .jsonPath("$.result.topics.edit.value[0]").exists()
        }

        @Test
        @DisplayName("GET /stats/country with statsTopics and topics")
        fun `get stats grouped by country with statsTopics and topics`() {
            val hashtag = "&*"

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/country")
                    .queryParam("topics", "edit,building")
                    .queryParam("hashtag", hashtag)
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result.topics.building").exists()
                .jsonPath("$.result.topics.edit").exists()
                .jsonPath("$.result.topics.edit[0].value").isEqualTo(1)
        }


        @Test
        @DisplayName("GET /stats/h3")
        fun `get statsH3 for edit`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/h3")
                    .queryParam("topic", "edit")
                    .build()
            }

            assertTrue(
                doGetAndAssertThat(url)
                    .returnResult().toString().contains("hex_cell")
            )

        }

        @Test
        @DisplayName("GET /stats/h3 with invalid topic name throws bad request")
        fun `get statsH3 with invalid topic name throws bad request`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/h3")
                    .queryParam("topic", "edits")
                    .build()
            }

            assertBadRequestResponse(url)

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
        @DisplayName("GET /stats/user with good token and statsTopics and topics")
        fun `get userstats with good token`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/user")
                    .queryParam("userId", "2186388")
                    .queryParam("hashtag", "hotosm-project-*")
                    .queryParam("topics", listOf("edit", "road"))
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

            println(response.returnResult())

            response
                .jsonPath("$.result.topics").exists()
                .jsonPath("$.result.topics.edit").exists()
                .jsonPath("$.result.topics.road").exists()
        }

        @Test
        @DisplayName("GET /stats/user with no hashtag")
        fun `get userstats with no hashtag`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/user")
                    .queryParam("userId", "2186388")
                    .queryParam("topics", listOf("road", "building"))
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

            println(response.returnResult())

            response
                .jsonPath("$.result.topics.road.value").isEqualTo(0.0)
                .jsonPath("$.result.topics.road.modified.unit_more").isEqualTo(0.0)
                .jsonPath("$.result.topics.building.added").isEqualTo(0.0)
        }

        @Test
        @DisplayName("GET /stats/user/interval")
        fun `get user stats by interval`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/user/interval")
                    .queryParam("userId", "552187")
                    .queryParam("topics", listOf("edit", "building"))
                    .queryParam("interval", "P1Y")
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

            println(response.returnResult())
            response
                .jsonPath("$.result.topics.building.added").isArray()
                .jsonPath("$.result.topics.edit.value").isArray()
        }

        @Test
        @DisplayName("GET /stats/user/country")
        fun `get user stats by country`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/user/country")
                    .queryParam("userId", "552187")
                    .queryParam("topics", listOf("edit", "building"))
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

            println(response.returnResult())
            response
                .jsonPath("$.result.topics.building[0].added").isEqualTo(1)
                .jsonPath("$.result.topics.building[0].modified.count_modified").isEqualTo(1)
                .jsonPath("$.result.topics.edit[1].value").isEqualTo(8)
        }


        @Test
        @DisplayName("GET /stats/user/h3")
        fun `get user stats by h3`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/stats/user/h3")
                    .queryParam("userId", "552187")
                    .queryParam("topic", "edit")
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

            println(response.returnResult())
            response.toString().contains("hex_cell")
        }

    }


    @Nested
    @DisplayName("for topic queries")
    @WithTopicData
    inner class TopicTests {

        val topic1 = "place"
        val topic2 = "healthcare"
        val topic3 = "waterway"
        val topics = listOf(topic1, topic2, topic3)


        @Test
        @DisplayName("GET /topic/definition/ for place and healthcare")
        fun `get topic definition for place and healthcare`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/definition")
                    .queryParam("topics", topics.joinToString(separator = ","))
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result").isMap
                .jsonPath("$.result.$topic1").exists()
                .jsonPath("$.result.$topic2").exists()
                .jsonPath("$.result.lulc").doesNotExist()
                .jsonPath("$.result.$topic1")
                .isEqualTo("place in (country, state, region, province, district, county, municipality, city, borough, suburb, quarter, neighbourhood, town, village, hamlet, isolated_dwelling)")
        }

        @Test
        @DisplayName("GET /topic/definition/ for all topics")
        fun `get topic definition for all topics`() {

            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/topic/definition")
                    .build()
            }

            doGetAndAssertThat(url)
                .jsonPath("$.result").isMap
                .jsonPath("$.result.$topic1").exists()
                .jsonPath("$.result.$topic2").exists()
                .jsonPath("$.result.lulc").exists()
                .jsonPath("$.result.edit").exists()
                .jsonPath("$.result.$topic1")
                .isEqualTo("place in (country, state, region, province, district, county, municipality, city, borough, suburb, quarter, neighbourhood, town, village, hamlet, isolated_dwelling)")
        }
    }

    @Nested
    @DisplayName("for meta queries")
    @WithStatsData
    inner class MetaQueries {
        @Test
        fun `metadata request always returns seconds`() {
            val url = { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/metadata")
                    .build()
            }

            val response = client()
                .get()
                .uri(url)
                .exchange()
                .expectStatus()
                .isOk
                .expectBody()

            response
                .jsonPath("$.result.max_timestamp").isEqualTo("2023-06-29T12:50:00Z")

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




