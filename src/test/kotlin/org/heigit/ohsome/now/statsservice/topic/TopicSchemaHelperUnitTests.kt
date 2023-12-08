package org.heigit.ohsome.now.statsservice.topic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TopicSchemaHelperUnitTests {


    private val amenityMatcher = KeyValueMatcher("amenity", listOf("doctors", "clinic")) // values not important here
    private val healthcareMatcher = KeyValueMatcher("healthcare", listOf("doctors", "clinic")) // values not important here


    @Test
    fun `can generate topic table DDL for single key and INT stage`() {

        val expected = file("create_topic_table_for_1_key")

        val definition =  KeyValueTopicDefinition("amenity", listOf(amenityMatcher))

        val sql = createTableDDL(definition)


        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    @Test
    fun `can generate topic table DDL for two keys and INT stage`() {

        val expected = file("create_topic_table_for_2_keys")

        val definition =  KeyValueTopicDefinition("healthcare", listOf(healthcareMatcher, amenityMatcher))

        val sql = createTableDDL(definition)

        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }



}


