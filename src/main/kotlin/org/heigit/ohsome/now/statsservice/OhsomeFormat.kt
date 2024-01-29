package org.heigit.ohsome.now.statsservice

import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.statsservice.utils.makeUrl
import org.springframework.web.servlet.HandlerMapping
import java.time.Instant
import java.time.temporal.ChronoUnit


fun <T> buildOhsomeFormat(results: Measured<T>, httpServletRequest: HttpServletRequest) = buildOhsomeFormat(
    results.result,
    results.executionTime,
    httpServletRequest
)


@Suppress("LongMethod", "ComplexMethod")
private fun <T> buildOhsomeFormat(
    results: T,
    executionTime: Long,
    httpServletRequest: HttpServletRequest
): OhsomeFormat<T> {
    val pathVariables = httpServletRequest
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>

    val query = QueryInfo(
        Timespan(
            httpServletRequest.getParameter("startdate") ?: Instant.EPOCH.toString(),
            httpServletRequest.getParameter("enddate") ?: Instant.now().truncatedTo(ChronoUnit.SECONDS).toString(),
            httpServletRequest.getParameter("interval"),
        ),
        pathVariables?.get("hashtag") ?: httpServletRequest.getParameter("hashtag"),
        pathVariables?.get("hashtags")?.split(','),
        httpServletRequest.getParameter("limit")?.toInt(),
        httpServletRequest.getParameter("countries")?.split(','),
        pathVariables?.get("topics")?.split(','),
        httpServletRequest.getParameter("userId")?.toInt(),
    )

    val metadata = Metadata(executionTime, makeUrl(httpServletRequest))

    val attribution = Attribution()

    return OhsomeFormat(results, attribution, metadata, query)
}


data class OhsomeFormat<T>(
    val result: T,
    val attribution: Attribution,
    val metadata: Metadata,
    val query: QueryInfo
)


data class Metadata(
    val executionTime: Long,
    val requestUrl: String,
    val apiVersion: String = "0.1"
)

data class Attribution(
    val url: String = "https://ohsome.org/copyrights",
    val text: String = "© OpenStreetMap contributors"
)

data class Timespan(
    val startDate: String,
    val endDate: String,
    val interval: String?
)

data class QueryInfo(
    val timespan: Timespan,
    val hashtag: String?,
    val hashtags: List<String>?,
    val limit: Int?,
    val countries: List<String>?,
    val topics: List<String>?,
    val userId: Int?,
)
