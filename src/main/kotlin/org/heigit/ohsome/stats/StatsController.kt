package org.heigit.ohsome.stats

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


// static data taken from http://osm-stats-production-api.azurewebsites.net/stats at 2pm, 20 March 2023


@RestController
class StatsController {


    @GetMapping("/stats")
    @Operation(summary = "Returns a static snapshot of OSM statistics (for now)")
    fun stats() = mapOf(
        "changesets" to 65009011,
        "users" to 3003842,
        "roads" to 45964973.0494135,
        "buildings" to 844294167,
        "edits" to 1095091515,
        "latest" to "2023-03-20T10:55:38.000Z",
        "hashtag" to "*"
    )


}
