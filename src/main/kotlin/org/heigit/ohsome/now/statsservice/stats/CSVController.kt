package org.heigit.ohsome.now.statsservice.stats

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi.create
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.sql.DataSource


// EXPERIMENTAL
@Component
class CSVController {

    @Autowired
    lateinit var dataSource: DataSource


    // the format `CSVWithNames` does not seem to work with this clickhouse version
    val sql = "SELECT arrayJoin([1, 2]), 3, arrayJoin([2, 7]) FORMAT CSV"


//    TODO: the real query:
//    SELECT
//    count(DISTINCT user_id) AS users,
//    count(map_feature_edit) AS edits,
//    h3ToString(h3_r3) as hex
//    FROM all_stats_3
//    GROUP BY h3_r3
//    LIMIT 10
//    FORMAT CSVWithNames
//    ;




    fun getCSVRows(): List<String> = query {
        it.select(sql)
            .mapTo(String::class.java)
            .list()
    }


    private fun <T> query(queryFunction: (handle: Handle) -> T) = create(dataSource)
        .withHandle<T, RuntimeException>(queryFunction)


}
