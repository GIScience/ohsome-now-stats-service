package org.heigit.ohsome.now.statsservice.topic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TopicSchemaHelperUnitTests {



    @Test
    fun `can generate topic table DDL for INT stage`() {

        val expected = file("create_topic_table_for_1_key")

        val matcher = KeyValueMatcher("amenity", listOf("doctors", "clinic")) // values not important here
        val definition =  KeyValueTopicDefinition("amenity", listOf(matcher))


        val key = matcher.key


        val sql =
        """
            CREATE TABLE IF NOT EXISTS int.topic_${definition.topic}
            (
                `changeset_timestamp` DateTime,
                `hashtag`             String,
                `user_id`             Int32,
                `country_iso_a3`      Array(String),
                `${key}_current`      String,
                `${key}_before`       String
            )
                ENGINE = MergeTree
                PRIMARY KEY( hashtag, changeset_timestamp)
            ;
        """.trimIndent()


        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


}


