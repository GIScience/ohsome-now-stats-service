package apiPerformance

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http


class TopicEndPointsSimulation : Simulation() {
    private val hashtags = listOf("missingmaps", "hotosm-project-*")
    private val topics = listOf("healthcare", "highway", "education", "waterway", "poi")
    private val feeder = listFeeder(
        startDateList.indices.flatMap { index ->
            hashtags.map { hashtag ->
                topics.map { topic ->
                    mapOf(
                        "startdate" to startDateList[index],
                        "enddate" to endDateList[index],
                        "hashtag" to hashtag,
                        "topic" to topic
                    )
                }
            }.flatten()
        }
    ).random()


    //combination of all the scenarios without pauses
    val DashBoardScenario = scenario("Dashboard Scenario")
        .feed(feeder).exec(
            http("Topic").get("/topic/#{topic}")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
                .queryParam("hashtag", "#{hashtag}")
        ).exec(
            http("Topic Interval").get("/stats/#{topic}/interval")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
                .queryParam("hashtag", "#{hashtag}")
        ).exec(
            http("Topic Country").get("/stats/#{topic}/country")
                .queryParam("startdate", "#{startdate}")
                .queryParam("enddate", "#{enddate}")
                .queryParam("hashtag", "#{hashtag}")
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