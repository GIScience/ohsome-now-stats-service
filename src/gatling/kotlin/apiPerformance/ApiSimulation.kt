package apiPerformance

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class ApiSimulation : Simulation() {

    val httpProtocol = http
        .baseUrl("http://localhost:8080")

    val usersPerSecond = 10.0
    val rampUpDuration = 60

    val statsStaticScenario = scenario("Stats Static Scenario")
        .exec(http("Stats Static").get("/stats_static"))

    val statsScenario = scenario("Stats Scenario")
        .exec(
            http("Stats").get("/stats/{hashtag}")
                .queryParam("hashtag", "missingmaps")
                .queryParam("startdate", "2022-01-01T00:00:00Z")
                .queryParam("enddate", "2022-01-31T23:59:59Z")
        )

    val metadataScenario = scenario("Metadata Scenario")
        .exec(http("Metadata").get("/metadata"))

    val allScenarios = listOf(
        statsStaticScenario,
        statsScenario,
        metadataScenario
    )

    init {
        setUp(
            statsStaticScenario.injectOpen(rampUsers(10).during(60), constantUsersPerSec(10.0).during(60)),
            statsScenario.injectOpen(rampUsers(10).during(60), constantUsersPerSec(10.0).during(60)),
            metadataScenario.injectOpen(rampUsers(10).during(60), constantUsersPerSec(10.0).during(60)),
        ).protocols(httpProtocol)
    }
}
