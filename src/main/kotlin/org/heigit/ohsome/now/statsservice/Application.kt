package org.heigit.ohsome.now.statsservice

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling


@OpenAPIDefinition(
    info = Info(
        title = "ohsome Contribution Stats Service: OpenAPI definition",
        description = "This document describes the REST endpoints for OSM contribution statistics.",
        version = "v1"
    )
)
@EnableCaching
@EnableScheduling
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
