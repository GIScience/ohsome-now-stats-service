package org.heigit.ohsome.now.stats.utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.servlet.HandlerMapping
import java.time.Instant
import java.time.temporal.ChronoUnit


fun makeUrl(request: HttpServletRequest): String {
    return request.requestURI.toString() + if (request.queryString != null) "?" + request.queryString else ""
}
