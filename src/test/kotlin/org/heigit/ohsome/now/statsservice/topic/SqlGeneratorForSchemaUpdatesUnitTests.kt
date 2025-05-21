package org.heigit.ohsome.now.statsservice.topic

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.file
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


@DisplayName("can generate")
class SqlGeneratorForSchemaUpdatesUnitTests {

    private val statsSchemaVersion = "7"
    private val topicSchemaVersion = "6"
    private val stage = "int"

    private val dateTime = "2023-06-15 17:00:00"

    val amenityMatcher = KeyValueMatcher("amenity", listOf("doctors", "clinic")) // values not important here
    val healthcareMatcher = KeyValueMatcher("healthcare", listOf("doctors", "clinic")) // values not important here

    //stats
    val expectedTableStats = file("create_stats_table")
    val expectedProjections = file("add_projections_to_stats_table")
    val expectedTableForHashtagAggregation = file("create_stats_table_for_hashtag_aggregation")
    val expectedMVForHashtagAggregation = file("create_stats_mv_for_hashtag_aggregation")
    val expectedTableForStatsUser = file("create_stats_user_table")
    val expectedMVForStatsUser = file("create_stats_user_mv_for_user_table")
    val expectedInsertForStatsUser = file("create_stats_user_insert")


    //topics
    val expectedTable1Key = file("create_topic_table_for_1_key")
    val expectedTable2Keys = file("create_topic_table_for_2_keys")
    val expectedTableUser = file("create_topic_user_table")


    val expectedMV1Key = file("create_topic_mv_for_1_key")
    val expectedMV2Keys = file("create_topic_mv_for_2_keys")
    val expectedMVUser = file("create_topic_user_mv")

    val expectedInsertStatement1Key = file("create_topic_insert_statement_for_1_key")
    val expectedInsertStatement2Keys = file("create_topic_insert_statement_for_2_keys")


    @Nested
    @DisplayName("stats table DDL")
    inner class StatsTableDDLTests {

        @Test
        fun `with correct schema`() {
            val sql = createStatsTableDDL(stage, statsSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedTableStats)
        }

    }

    @Nested
    @DisplayName("stats projections/materialized views")
    inner class StatsProjectionAndMVTests {


        @Test
        fun `timestamp projection for INT stage`() {

            val sql = createStatsTableProjections(stage, statsSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedProjections)
        }

        @Test
        fun `hashtag aggregation table DDL`() {

            val sql = createStatsHashtagAggregationTable(stage, statsSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedTableForHashtagAggregation)
        }

        @Test
        fun `stats user table DDL`() {

            val sql = createStatsUserTable(stage, statsSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedTableForStatsUser)
        }

        @Test
        fun `hashtag aggregation MV DDL`() {

            val sql = createStatsMaterializedViewForHashtagAggregation(stage, statsSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedMVForHashtagAggregation)
        }

        @Test
        fun `stats user MV DDL`() {

            val sql = createStatsMaterializedViewForUserTable(stage, statsSchemaVersion, dateTime)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedMVForStatsUser)
        }

        @Test
        fun `stats user insert`() {

            val sql = createStatsUserTableInsert(stage, statsSchemaVersion, dateTime)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertForStatsUser)
        }

    }


    @Nested
    @DisplayName("topic table DDL")
    inner class TableDDLTests {


        @Test
        fun `for single key and INT stage - key-value definition`() {

            val definition = TopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createTopicTableDDL(definition, stage, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTable1Key)
        }


        @Test
        fun `for single key and INT stage - key-only definition`() {

            val definition = TopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")))


            val sql = createTopicTableDDL(definition, stage, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTable1Key)
        }


        @Test
        fun `for two keys and INT stage`() {

            val definition = TopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

            val sql = createTopicTableDDL(definition, stage, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTable2Keys)
        }

        @Test
        fun `for topic user INT stage`() {

            val definition = TopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createTopicUserTableDDL(definition, stage, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTableUser)
        }


    }


    @Nested
    @DisplayName("topic MV DDL")
    inner class MVDDLTests {


        @Test
        fun `for single key and INT stage - key-value definition`() {

            val definition = TopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createTopicMvDDL(definition, dateTime, stage, statsSchemaVersion, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedMV1Key)
        }


        @Test
        fun `for single key and INT stage - key-only definition`() {

            val definition = TopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")))

            val sql = createTopicMvDDL(definition, dateTime, stage, statsSchemaVersion, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedMV1Key)
        }


        @Test
        fun `for two keys and INT stage`() {

            val definition = TopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

            val sql = createTopicMvDDL(definition, dateTime, stage, statsSchemaVersion, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedMV2Keys)
        }

        @Test
        fun `for user table`() {

            val definition = TopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createTopicUserMvDDL(definition, stage, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedMVUser)
        }

    }

    @Nested
    @DisplayName("topic insert statement")
    inner class InsertStatementTests {

        @Test
        fun `for single key and INT stage - key-value definition`() {

            val definition = TopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createTopicInsertStatement(definition, dateTime, stage, statsSchemaVersion, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertStatement1Key)
        }

        @Test
        fun `for single key and INT stage - key-only definition`() {

            val definition = TopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")))


            val sql = createTopicInsertStatement(definition, dateTime, stage, statsSchemaVersion, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertStatement1Key)
        }


        @Test
        fun `for two keys and INT stage`() {

            val definition = TopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

            val sql = createTopicInsertStatement(definition, dateTime, stage, statsSchemaVersion, topicSchemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertStatement2Keys)
        }


    }


}


