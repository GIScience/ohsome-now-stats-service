package org.heigit.ohsome.stats

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi.create
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource


@Component
class StatsRepo {


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
        FROM "stats"
        WHERE
            hashtag = ?;
        """.trimIndent()


    fun getStats(hashtag: String) = create(dataSource)
        .withHandle<Map<String, Any>, RuntimeException> { asMap(it, hashtag) }


    private fun asMap(handle: Handle, hashtag: String) = handle
        .select(sql, hashtag)
        .mapToMap()
        .single()

}
