package apiPerformance

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class EndPointsSimulation : Simulation() {

    val userPerSecodn = 10
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

    val StatsIntervalScenario = scenario("Stats Interval Scenario")
        .feed(feeder)
        .exec(
            http("Stats Interval").get("/stats/#{hashtag}/interval")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        )

    val StatsCountryScenario = scenario("Stats Country Scenario")
        .feed(feeder)
        .exec(
            http("Stats Country").get("/stats/#{hashtag}/country")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        )

    val MostUsedHashtagsScenario = scenario("Most Used Hashtags Scenario")
        .feed(feeder).exec(
            http("Most Used Hashtags").get("/most-used-hashtags")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
        )

    //combination of all the scenarios without pauses
    val dashBoardScenario = scenario("Dashboard Scenario")
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

    val httpProtocol = http
        .baseUrl("http://localhost:8080")

    val scenarios = listOf(
        StatsScenario,
        StatsIntervalScenario,
        StatsCountryScenario,
        MostUsedHashtagsScenario,
        dashBoardScenario
    )

    init {
        val injections = scenarios.map { scenario ->
            scenario.injectOpen(
                rampUsers(userPerSecodn).during(rampUpDuration.toLong()),
                constantUsersPerSec(userPerSecodn.toDouble()).during(constantDuration.toLong())
            )
        }
        setUp(*injections.toTypedArray()).protocols(httpProtocol)
    }
}