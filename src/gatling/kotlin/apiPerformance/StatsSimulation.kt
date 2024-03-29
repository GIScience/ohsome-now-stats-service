package apiPerformance

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class StatsSimulation : Simulation() {
    private val hashtags = listOf("hotosm-project-*")

    private val feeder = listFeeder(
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

    private val statsScenario = scenario("Stats Scenario")
        .feed(feeder)
        .exec(
            http("Stats").get("/stats/#{hashtag}")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        )

    private val httpProtocol = http
        .baseUrl("https://int-stats.now.ohsome.org/api/")

    private val scenarios = listOf(statsScenario)

    init {
        val injections = scenarios.map { scenario ->
            scenario.injectOpen(
                rampUsers(usersPerSecond).during(rampUpDuration),
                constantUsersPerSec(usersPerSecond.toDouble()).during(constantDuration)
            )
        }
        setUp(*injections.toTypedArray()).protocols(httpProtocol)
    }
}
