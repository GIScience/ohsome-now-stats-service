package org.heigit.ohsome.now.stats.models

import jakarta.servlet.http.HttpServletRequest
import org.heigit.ohsome.now.stats.Measured
import org.heigit.ohsome.now.stats.utils.makeUrl
import org.springframework.web.servlet.HandlerMapping
import java.time.Instant
import java.time.temporal.ChronoUnit


fun <T> buildOhsomeFormat(results: Measured<T>, httpServletRequest: HttpServletRequest) = buildOhsomeFormat(
    results.result,
    results.executionTime,
    httpServletRequest
)


private fun <T> buildOhsomeFormat(results: T, executionTime: Long, httpServletRequest: HttpServletRequest): OhsomeFormat<T> {
    val pathVariables = httpServletRequest
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>

    val query = QueryInfo(
        Timespan(
            httpServletRequest.getParameter("startdate") ?: Instant.EPOCH.toString(),
            httpServletRequest.getParameter("enddate") ?: Instant.now().truncatedTo(ChronoUnit.SECONDS).toString(),
            httpServletRequest.getParameter("interval"),
        ),
        pathVariables?.get("hashtag"),
        httpServletRequest.getParameter("limit")?.toInt(),
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
    val limit: Int?,
)
