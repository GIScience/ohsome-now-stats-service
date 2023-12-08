package org.heigit.ohsome.now.statsservice.topic


fun createTableDDL(definition: TopicDefinition): String {


    val keyColumns = definition
        .keys()
        .map(::columnDefinitions)
        .joinToString(separator = ",\n")


    return """
            CREATE TABLE IF NOT EXISTS int.topic_${definition.topicName}
            (
                `changeset_timestamp` DateTime,
                `hashtag`             String,
                `user_id`             Int32,
                `country_iso_a3`      Array(String),
                $keyColumns
            )
                ENGINE = MergeTree
                PRIMARY KEY( hashtag, changeset_timestamp)
            ;
        """.trimIndent()
}

private fun columnDefinitions(key: String) = """
        `${key}_current`      String, 
        `${key}_before`       String"""



