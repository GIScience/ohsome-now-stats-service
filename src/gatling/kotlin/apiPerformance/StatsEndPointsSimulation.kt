package apiPerformance

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http


class StatsEndPointsSimulation : Simulation() {
    private val hashtags = listOf("missingmaps", "visa")

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
    

    //combination of all the scenarios without pauses
    val DashBoardScenario = scenario("Dashboard Scenario")
        .feed(feeder).exec(
            http("Stats").get("/stats/#{hashtag}")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        ).exec(
            http("Stats Interval").get("/stats/#{hashtag}/interval")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        ).exec(
            http("Stats Country").get("/stats/#{hashtag}/country")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        ).exec(
            http("Most Used Hashtags").get("/most-used-hashtags")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        )


    private val scenarios = listOf(
        DashBoardScenario
    )

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