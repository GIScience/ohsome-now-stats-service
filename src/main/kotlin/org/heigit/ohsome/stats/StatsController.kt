package org.heigit.ohsome.stats

import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController



@RestController
class StatsController {


    @Autowired
    lateinit var repo: StatsRepo


    @Operation(summary = "Returns live data from DB")
    @GetMapping("/stats/{hashtag}")
    fun statsDB(@PathVariable hashtag: String, @RequestParam("startdate", required = false) startDate: String?): Map<String, Any> {

        if (startDate == null)
            return this.repo.getStats(hashtag)

        return this.repo.getStats(hashtag) + mapOf("startdate" to startDate)
    }


    // static data taken from http://osm-stats-production-api.azurewebsites.net/stats at 2pm, 20 March 2023
    @Operation(summary = "Returns a static snapshot of OSM statistics (for now)")
    @GetMapping("/stats_static")
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
