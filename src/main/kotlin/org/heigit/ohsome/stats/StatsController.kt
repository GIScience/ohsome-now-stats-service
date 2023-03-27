package org.heigit.ohsome.stats

import io.swagger.v3.oas.annotations.Operation
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource


// static data taken from http://osm-stats-production-api.azurewebsites.net/stats at 2pm, 20 March 2023


//TODO: https://www.baeldung.com/spring-boot-jdbi
@RestController
class StatsController {


    @Autowired
    lateinit var dataSource: DataSource


    //language=SQL
    val sql = """
        SELECT

            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length) as roads,
            count(building_area) as buildings,
            count(*) as edits,
            FROM_UNIXTIME(intDiv(max(changeset_timestamp), 1000)) as latest

        FROM "__stats_all_unnested"
        WHERE
            hashtag = '#missingmaps';
        """.trimIndent()


    @GetMapping("/stats_db")
    @Operation(summary = "Returns live data from DB")
    fun statsDB(): Map<String, Any> {

        val jdbi = Jdbi.create(dataSource)

        return jdbi.withHandle<Map<String, Any>, RuntimeException>(::asMap)

    }


    private fun asMap(handle: Handle) = handle
        .createQuery(sql)
        .mapToMap()
        .single()


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
