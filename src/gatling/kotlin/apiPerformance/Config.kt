package apiPerformance

import io.gatling.javaapi.http.HttpDsl

const val usersPerSecond = 5
const val rampUpDuration = 30L
const val constantDuration = 30L

val httpProtocol = HttpDsl.http
    .baseUrl("https://int-stats.now.ohsome.org/api/")


val startDateList = listOf(
    "2022-01-01T00:00:00Z",
    "2022-01-01T00:00:00Z",
    "2008-01-01T00:00:00Z",
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
