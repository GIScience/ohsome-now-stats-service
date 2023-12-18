package org.heigit.ohsome.now.statsservice.topic

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.file
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


@DisplayName("can generate")
class SqlGeneratorForSchemaUpdatesUnitTests {

    private val schemaVersion = "7"
    private val stage = "int"

    private val dateTime = "2023-06-15 17:00:00"

    val amenityMatcher = KeyValueMatcher("amenity", listOf("doctors", "clinic")) // values not important here
    val healthcareMatcher = KeyValueMatcher("healthcare", listOf("doctors", "clinic")) // values not important here


    val expectedTable1Key = file("create_topic_table_for_1_key")
    val expectedTable2Keys = file("create_topic_table_for_2_keys")

    val expectedMV1Key = file("create_topic_mv_for_1_key")
    val expectedMV2Keys = file("create_topic_mv_for_2_keys")

    val expectedInsertStatement1Key = file("create_topic_insert_statement_for_1_key")
    val expectedInsertStatement2Keys = file("create_topic_insert_statement_for_2_keys")


    val expectedProjections = file("add_projections_to_stats_table")


    @Nested
    @DisplayName("projections for stats table")
    inner class StatsProjectionTests {


        @Test
        fun `timestamp and user id projections for INT stage`() {

            val sql = createStatsTableProjections(stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedProjections)
        }


    }


    @Nested
    @DisplayName("topic table DDL")
    inner class TableDDLTests {


        @Test
        fun `for single key and INT stage - key-value definition`() {

            val definition = KeyValueTopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createTableDDL(definition, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTable1Key)
        }


        @Test
        fun `for single key and INT stage - key-only definition`() {

            val definition = KeyValueTopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")))


            val sql = createTableDDL(definition, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTable1Key)
        }


        @Test
        fun `for two keys and INT stage`() {

            val definition = KeyValueTopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

            val sql = createTableDDL(definition, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedTable2Keys)
        }

    }


    @Nested
    @DisplayName("topic MV DDL")
    inner class MVDDLTests {


        @Test
        fun `for single key and INT stage - key-value definition`() {

            val definition = KeyValueTopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createMvDdl(definition, dateTime, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedMV1Key)
        }


        @Test
        fun `for single key and INT stage - key-only definition`() {

            val definition = KeyValueTopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")))

            val sql = createMvDdl(definition, dateTime, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingPunctuationAndWhitespace(expectedMV1Key)
        }


        @Test
        fun `for two keys and INT stage`() {

            val definition = KeyValueTopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

            val sql = createMvDdl(definition, dateTime, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedMV2Keys)
        }

    }

    @Nested
    @DisplayName("topic insert statement")
    inner class InsertStatementTests {

        @Test
        fun `for single key and INT stage - key-value definition`() {

            val definition = KeyValueTopicDefinition("amenity", listOf(amenityMatcher))

            val sql = createInsertStatement(definition, dateTime, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertStatement1Key)
        }

        @Test
        fun `for single key and INT stage - key-only definition`() {

            val definition = KeyValueTopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")))


            val sql = createInsertStatement(definition, dateTime, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertStatement1Key)
        }


        @Test
        fun `for two keys and INT stage`() {

            val definition = KeyValueTopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

            val sql = createInsertStatement(definition, dateTime, stage, schemaVersion)
            assertThat(sql)
                .isEqualToNormalizingWhitespace(expectedInsertStatement2Keys)
        }


    }


}


