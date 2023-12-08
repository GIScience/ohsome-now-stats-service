package org.heigit.ohsome.now.statsservice.topic


fun createMVDDL(definition: TopicDefinition): String {

    val keyColumns = keyColumns(definition)
    val whereClause = whereClause(definition)

    return """
        CREATE MATERIALIZED VIEW int.mv__stats_to_topic_${definition.topicName} TO int.topic_${definition.topicName}
        AS SELECT
        (
            `changeset_timestamp`,
            `hashtag`,
            `user_id`,
            `country_iso_a3`,
            $keyColumns
        )
        FROM int.stats
        WHERE
            $whereClause
            """.trimIndent().trimMargin()
}


fun createTableDDL(definition: TopicDefinition): String {


    val keyColumnDefinitions = keyColumnDefinitions(definition)


    return """
            CREATE TABLE IF NOT EXISTS int.topic_${definition.topicName}
            (
                `changeset_timestamp` DateTime,
                `hashtag`             String,
                `user_id`             Int32,
                `country_iso_a3`      Array(String),
                $keyColumnDefinitions
            )
                ENGINE = MergeTree
                PRIMARY KEY( hashtag, changeset_timestamp)
            ;
        """.trimIndent()
}



private fun whereClause(definition: TopicDefinition) = definition
    .keys()
    .map(::whereClauseParts)
    .joinToString(separator = "\nOR\n")


private fun keyColumns(definition: TopicDefinition) = definition
    .keys()
    .map(::columnNames)
    .joinToString(separator = ",\n")


private fun keyColumnDefinitions(definition: TopicDefinition) = definition
    .keys()
    .map(::columnDefinitions)
    .joinToString(separator = ",\n")


private fun columnDefinitions(key: String) = """
        `${key}_current`      String, 
        `${key}_before`       String"""


private fun columnNames(key: String) = """
        tags['${key}'] as  `${key}_current`, 
        tags_before['${key}'] as `${key}_before`"""


private fun whereClauseParts(key: String) = """
        tags['${key}']  != '' OR tags_before['${key}'] != '' """




