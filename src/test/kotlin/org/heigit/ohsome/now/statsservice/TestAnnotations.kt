package org.heigit.ohsome.now.statsservice

import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import javax.sql.DataSource
import kotlin.annotation.AnnotationRetention.RUNTIME


@Retention(RUNTIME)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
annotation class SpringTestWithClickhouse


open class SqlPopulatorExtension(private vararg val scripts: String) : BeforeEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        val dataSource = SpringExtension.getApplicationContext(context).getBean<DataSource>()
        ResourceDatabasePopulator(*scripts.map { ClassPathResource(it) }.toTypedArray())
            .execute(dataSource)
    }
}


@Retention(RUNTIME)
@ExtendWith(
    WithStatsDataExtension::class
)
annotation class WithStatsData

@Retention(RUNTIME)
@ExtendWith(
    WithTopicDataExtension::class
)
annotation class WithTopicData

class WithStatsDataExtension : SqlPopulatorExtension(
    "/db_setup/stats/init_stats_schema.sql",
    "/db_setup/stats/init_stats_user_schema.sql",
    "/db_setup/stats/init_hashtag_aggregation_schema.sql",
    "/db_setup/stats/stats_400rows.sql",
    "/db_setup/stats/stats_user_400rows.sql",
    "/db_setup/stats/hashtag_aggregation_rows.sql",
    "/db_setup/topics/init_schema_building_view.sql",
    "/db_setup/topics/init_schema_road_view.sql",
    "/db_setup/topics/init_schema_building_user_view.sql",
    "/db_setup/topics/init_schema_road_user_view.sql",
    "/db_setup/topics/topic_building_20rows.sql",
    "/db_setup/topics/topic_road_40rows.sql",
    "/db_setup/topics/topic_building_user_20rows.sql",
    "/db_setup/topics/topic_road_user_40rows.sql",
)

class WithTopicDataExtension : SqlPopulatorExtension(
    "/db_setup/topics/init_schema_place_view.sql",
    "/db_setup/topics/init_schema_healthcare_view.sql",
    "/db_setup/topics/init_schema_amenity_view.sql",
    "/db_setup/topics/init_schema_waterway_view.sql",
    "/db_setup/topics/init_schema_building_view.sql",
    "/db_setup/topics/init_schema_road_view.sql",
    "/db_setup/topics/topic_place_40rows.sql",
    "/db_setup/topics/topic_healthcare_40rows.sql",
    "/db_setup/topics/topic_amenity_40rows.sql",
    "/db_setup/topics/topic_waterway_40rows.sql",
    "/db_setup/topics/topic_building_20rows.sql",
    "/db_setup/topics/topic_road_40rows.sql",
    "/db_setup/topics/init_schema_building_user_view.sql",
    "/db_setup/topics/init_schema_road_user_view.sql",
    "/db_setup/topics/init_schema_place_user_view.sql",
    "/db_setup/topics/init_schema_healthcare_user_view.sql",
    "/db_setup/topics/topic_building_user_20rows.sql",
    "/db_setup/topics/topic_road_user_40rows.sql",
    "/db_setup/topics/topic_place_user_40rows.sql",
    "/db_setup/topics/topic_healthcare_user_40rows.sql",
)