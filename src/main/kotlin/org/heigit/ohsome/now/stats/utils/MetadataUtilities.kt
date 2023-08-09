package org.heigit.ohsome.now.stats.utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.HandlerMapping
import java.time.Instant
import java.time.temporal.ChronoUnit


fun makeUrl(request: HttpServletRequest): String {
    return request.requestURI.toString() + "?" + (request.queryString ?: "")
}


private fun buildMetadata(executionTime: Long, httpServletRequest: HttpServletRequest): Map<String, Any> {
    return mapOf(
        "executionTime" to executionTime,
        "requestUrl" to makeUrl(httpServletRequest),
        "apiVersion" to "tbd"

    )
}

private fun buildAttribution(): Map<String, String> {
    return mapOf("url" to "https://ohsome.org/copyrights", "text" to "© OpenStreetMap contributors")
}

@Suppress("LongParameterList")
private fun buildQueryInfoTimespan(
    startDate: String?,
    endDate: String?,
    hashtag: String? = null,
    interval: String? = null,
    limit: Int? = null
): Map<String, Any?> {
    val timespan = mapOf(
        "startDate" to (startDate ?: Instant.EPOCH.toString()),
        "endDate" to (endDate ?: Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()),
        "interval" to interval
    ).filterValues { it != null }

    val queryInfo = mapOf(
        "timespan" to timespan,
        "hashtag" to hashtag,
        "limit" to limit
    ).filterValues { it != null }

    return queryInfo
}


/**
 * Echoes the request parameters as a map.
 *
 * @param startDate the (inclusive) start date for the query
 * @param endDate the (exclusive) end date for the query
 * @return a map containing the request parameters
 */
fun echoRequestParameters(startDate: Instant?, endDate: Instant?): Map<String, Instant> {
    val extraParams = mutableMapOf<String, Instant>()
    startDate?.let { extraParams["startdate"] = it }
    endDate?.let { extraParams["enddate"] = it }
    return extraParams
}


fun buildOhsomeFormat(stats: Any, executionTime: Long, httpServletRequest: HttpServletRequest): Map<String, Any> {
    val pathVariables =
        httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>
    return mutableMapOf(
        "result" to stats,
        "attribution" to buildAttribution(),
        "metadata" to buildMetadata(executionTime, httpServletRequest),
        "query" to buildQueryInfoTimespan(
            httpServletRequest.getParameter("startdate"),
            httpServletRequest.getParameter("enddate"),
            pathVariables?.get("hashtag"),
            httpServletRequest.getParameter("interval"),
            httpServletRequest.getParameter("limit")?.toInt(),
        )
    )
}

/**
 * If only one results was provided, unnest it behind from its hashtag.
 *
 * @param results results for 1 to X hashtags
 * @return results, potentially unnested
 */
fun checkIfOnlyOneResult(results: MutableMap<String, Map<String, Any>>): Map<String, Any> {
    return if (results.size == 1) {
        results.values.iterator().next()
    } else {
        results
    }
}