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



private fun keyColumns(definition: TopicDefinition) = createFromKeys(definition, ::columnNames)
private fun keyColumnDefinitions(definition: TopicDefinition) = createFromKeys(definition, ::columnDefinitions)
private fun whereClause(definition: TopicDefinition ) = createFromKeys(definition, ::whereClauseParts, "\nOR\n")


private fun createFromKeys(definition: TopicDefinition, transform: (String) -> String, separator: String = ",\n") = definition
    .keys()
    .map(transform)
    .joinToString(separator = separator)


private fun columnDefinitions(key: String) = """
        `${key}_current`      String, 
        `${key}_before`       String"""


private fun columnNames(key: String) = """
        tags['${key}'] as  `${key}_current`, 
        tags_before['${key}'] as `${key}_before`"""


private fun whereClauseParts(key: String) = """
        tags['${key}']  != '' OR tags_before['${key}'] != '' """




