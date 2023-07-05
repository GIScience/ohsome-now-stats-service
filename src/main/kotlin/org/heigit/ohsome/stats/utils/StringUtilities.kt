package org.heigit.ohsome.stats.utils

import jakarta.servlet.http.HttpServletRequest


/**
 * Translates a custom ISO 8601 interval to the corresponding ClickHouse query for aggregator and grouper.
 *
 * @param interval The custom ISO 8601 interval string.
 * @return The aggregator string for the ClickHouse query.
 *         If the interval is not in the expected format, an empty string is returned.
 */
fun getGroupbyInterval(interval: String): String {
    if ("T" in interval) return interval.replaceTime()
    else return interval.replaceDate()
}


fun String.replaceDate() = this
    // Remove the start of the ISO string
    .replace("P", "")
    // Adjust the date section
    .replace("Y", " YEAR")
    .replace("M", " MONTH")
    .replace("W", " WEEK")
    .replace("D", " DAY")

fun String.replaceTime() = this
    // Remove the start of the ISO string
    .replace("PT", "")
    // Adjust the time section
    .replace("H", " HOUR")
    .replace("M", " MINUTE")
