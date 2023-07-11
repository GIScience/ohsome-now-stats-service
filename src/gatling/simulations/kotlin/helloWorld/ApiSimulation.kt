package helloWorld

import io.gatling.javaapi.core.*
import io.gatling.javaapi.http.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*


class ApiSimulation:Simulation() {

    val httpProtocol = http
        .baseUrl("http://localhost:8080")

    val statsStaticScenario = scenario("Stats Static Scenario")
        .exec(http("Stats Static").get("/stats_static"))

    setUp(
        statsStaticScenario.injectOpen(rampUsers(10).during(10))
    ).protocols(httpProtocol)
}
