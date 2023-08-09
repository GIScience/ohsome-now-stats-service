package org.heigit.ohsome.now.stats

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@OpenAPIDefinition(
    info = Info(
        title = "ohsome Contribution Stats Service: OpenAPI definition",
        description = "This document describes the REST endpoints for OSM contribution statistics.",
        version = "v1"
    )
)
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
