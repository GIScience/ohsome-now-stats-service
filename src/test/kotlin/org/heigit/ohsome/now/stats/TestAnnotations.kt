package org.heigit.ohsome.now.stats

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.annotation.AnnotationRetention.RUNTIME


@Retention(RUNTIME)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
annotation class SpringTestWithClickhouse { }


@Retention(RUNTIME)
@Sql(*["/stats/init_schema.sql", "/stats/stats_400rows.sql"])
annotation class WithStatsData { }


@Retention(RUNTIME)
@Sql(*[
    "/topics/init_schema_place_view.sql",
    "/topics/init_schema_healthcare_view.sql",
    "/topics/init_schema_amenity_view.sql",

    "/topics/topic_place_40rows.sql",
    "/topics/topic_healthcare_40rows.sql",
    "/topics/topic_amenity_40rows.sql"
])
annotation class WithTopicData { }


