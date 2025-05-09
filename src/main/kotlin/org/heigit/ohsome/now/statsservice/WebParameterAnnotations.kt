package org.heigit.ohsome.now.statsservice

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@Parameter(description = "the hashtag to query for - case-insensitive and without the leading '#'", example = "missingmaps")
annotation class HashtagConfig { }


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@DateTimeFormat(iso = DATE_TIME)
@Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)", example = "2024-01-01T00:00:00Z")
annotation class StartDateConfig { }


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@DateTimeFormat(iso = DATE_TIME)
@Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)", example = "2025-01-01T00:00:00Z")
annotation class EndDateConfig { }


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@Parameter(description = "A comma separated list of countries as ISO 3166-1 alpha-3 codes, can also only be one country, e.g. 'DEU'")
annotation class CountriesConfig {}
