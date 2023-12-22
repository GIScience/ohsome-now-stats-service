package org.heigit.ohsome.now.statsservice

import io.swagger.v3.oas.annotations.Parameter
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@DateTimeFormat(iso = DATE_TIME)
@Parameter(description = "the (inclusive) start date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
annotation class StartDateConfig { }


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
@DateTimeFormat(iso = DATE_TIME)
@Parameter(description = "the (exclusive) end date for the query in ISO format (e.g. 2020-01-01T00:00:00Z)")
annotation class EndDateConfig { }

