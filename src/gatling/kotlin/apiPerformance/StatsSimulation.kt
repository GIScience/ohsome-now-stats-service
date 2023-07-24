package apiPerformance

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class StatsSimulation : Simulation() {

    val usersPerSecond = 10
    val rampUpDuration = 60
    val constantDuration = 60

    val startDateList = listOf(
        "2022-01-01T00:00:00Z",
        "2022-01-01T00:00:00Z",
        "2022-01-01T00:00:00Z",
        "2022-01-01T00:00:00Z",
        "2017-01-01T00:00:00Z"
    )

    val endDateList = listOf(
        "2022-01-01T23:59:59Z",
        "2022-01-07T23:59:59Z",
        "2022-01-31T23:59:59Z",
        "2022-12-31T23:59:59Z",
        "2022-12-31T23:59:59Z"
    )

    val hashtags = listOf("missingmaps", "visa")

    val feeder = listFeeder(
        startDateList.indices.flatMap { index ->
            hashtags.map { hashtag ->
                mapOf(
                    "startdate" to startDateList[index],
                    "enddate" to endDateList[index],
                    "hashtag" to hashtag
                )
            }
        }
    ).random()

    val StatsScenario = scenario("Stats Scenario")
        .feed(feeder)
        .exec(
            http("Stats").get("/stats/#{hashtag}")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        )

    val httpProtocol = http
        .baseUrl("http://localhost:8080")

    val scenarios = listOf(StatsScenario)

    init {
        val injections = scenarios.map { scenario ->
            scenario.injectOpen(
                rampUsers(usersPerSecond.toInt()).during(rampUpDuration.toLong()),
                constantUsersPerSec(usersPerSecond.toDouble()).during(constantDuration.toLong())
            )
        }
        setUp(*injections.toTypedArray()).protocols(httpProtocol)
    }
}
