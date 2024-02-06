package org.heigit.ohsome.now.statsservice.utils

import org.heigit.ohsome.now.statsservice.ISO8601TooSmallException
import org.heigit.ohsome.now.statsservice.UnparsableISO8601StringException
import kotlin.time.Duration


/**
 * Translates a custom ISO 8601 interval to the corresponding ClickHouse query for aggregator and grouper.
 *
 * @param interval The custom ISO 8601 interval string.
 * @return The aggregator string for the ClickHouse query.
 *         If the interval is not in the expected format, an empty string is returned.
 */
fun getGroupbyInterval(interval: String): String {
    return if ("T" in interval) interval.replaceTime()
    else interval.replaceDate()
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


fun validateIntervalString(interval: String) {
//    checkIfStringIsParsable(interval)
    checkIfDurationIsBiggerThanOneMinute(interval)
}

@Deprecated("is handled via jakarta bean validation annotations now")
fun checkIfStringIsParsable(interval: String) {
    if (!isParseableISO8601String(interval)) {
        throw UnparsableISO8601StringException()
    }
}


fun isParseableISO8601String(interval: String) = interval
    .matches(Regex("^P(?!\$)(\\d+Y)?(\\d+M)?(\\d+W)?(\\d+D)?(T(?=\\d)(\\d+H)?(\\d+M)?(\\d+S)?)?\$"))


//TODO: consider replacing with jakarta bean validation annotations instead of throwing exception
fun checkIfDurationIsBiggerThanOneMinute(interval: String) {
    if (interval.startsWith("PT") && Duration.parseIsoString(interval) < Duration.parseIsoString("PT1M")) {
        throw ISO8601TooSmallException()
    }
}