package org.heigit.ohsome.stats

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi.create
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Instant.EPOCH
import java.time.Instant.now
import javax.sql.DataSource


@Component
class StatsRepo {


    @Autowired
    lateinit var dataSource: DataSource


    //language=SQL
    val stats = """
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

    val statsFromTimeSpan = """
        SELECT
            count(distinct changeset_id) as changesets,
            count(distinct user_id) as users,
            sum(road_length) as roads,
            count(building_area) as buildings,
            count(*) as edits,
            FROM_UNIXTIME(intDiv(max(changeset_timestamp), 1000)) as latest
        FROM "stats"
        WHERE
            hashtag = ? and changeset_timestamp > ? and changeset_timestamp < ?;
            
        """.trimIndent()


    fun getStats(hashtag: String) = create(dataSource)
        .withHandle<Map<String, Any>, RuntimeException> { asMap(it, "#$hashtag") } + ("hashtag" to hashtag)


    fun getStatsForTimeSpan(hashtag: String, startDate: Instant = EPOCH, endDate: Instant = now()) = create(dataSource)
        .withHandle<Map<String, Any>, RuntimeException> { asMapFromTimeSpan(it, "#$hashtag", startDate, endDate) } +
            ("hashtag" to hashtag)


    private fun asMap(handle: Handle, hashtag: String) = handle
        .select(stats, hashtag)
        .mapToMap()
        .single()

    private fun asMapFromTimeSpan(handle: Handle, hashtag: String, startDate: Instant, endDate: Instant) = handle
            .select(statsFromTimeSpan, hashtag, startDate.toEpochMilli(), endDate.toEpochMilli())
            .mapToMap()
            .single()
}
