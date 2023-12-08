package org.heigit.ohsome.now.statsservice.topic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TopicSchemaHelperUnitTests {


    private val amenityMatcher = KeyValueMatcher("amenity", listOf("doctors", "clinic")) // values not important here
    private val healthcareMatcher = KeyValueMatcher("healthcare", listOf("doctors", "clinic")) // values not important here


    private val expectedTable1Key = file("create_topic_table_for_1_key")
    private val expectedTable2Keys = file("create_topic_table_for_2_keys")

    private val expectedMV1Key = file("create_topic_mv_for_1_key")




    @Test
    fun `can generate topic MV DDL for single key and INT stage - key-value definition`() {

        val definition =  KeyValueTopicDefinition("amenity", listOf(amenityMatcher))

        val sql = createMVDDL(definition)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expectedMV1Key)
    }


    @Test
    fun `can generate topic table DDL for single key and INT stage - key-value definition`() {

        val definition =  KeyValueTopicDefinition("amenity", listOf(amenityMatcher))

        val sql = createTableDDL(definition)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expectedTable1Key)
    }


    @Test
    fun `can generate topic table DDL for single key and INT stage - key-only definition`() {

        val definition =  KeyOnlyTopicDefinition("amenity", "amenity")

        val sql = createTableDDL(definition)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expectedTable1Key)
    }


    @Test
    fun `can generate topic table DDL for two keys and INT stage`() {

        val definition =  KeyValueTopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

        val sql = createTableDDL(definition)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expectedTable2Keys)
    }



}


