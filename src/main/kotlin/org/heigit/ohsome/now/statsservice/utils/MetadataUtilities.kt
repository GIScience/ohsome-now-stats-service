package org.heigit.ohsome.now.statsservice.utils

import jakarta.servlet.http.HttpServletRequest


fun makeUrl(request: HttpServletRequest): String {
    return request.requestURI.toString() + if (request.queryString != null) "?" + request.queryString else ""
}
