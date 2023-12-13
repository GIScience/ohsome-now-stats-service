package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.LENGTH


@Suppress("LongMethod")
fun createInsertStatement(
    definition: TopicDefinition,
    dateTime: String,
    stage: String,
    tableName: String,
    generation: String
) = """
    INSERT into $stage.topic_${definition.topicName}_${generation}
    SELECT
        changeset_timestamp,
        user_id,
        hashtag,
        country_iso_a3,
        ${keyColumns(definition)}
        ${optionalAreaOrLengthColumnNames(definition)} 
    FROM
        $stage.${tableName}_${generation}
    WHERE
        changeset_timestamp <= parseDateTimeBestEffort('$dateTime')
        AND
        (
            ${whereClause(definition)}
        )
    """.trimIndent().trimMargin()


@Suppress("LongMethod")
fun createMvDdl(definition: TopicDefinition, dateTime: String, stage: String, tableName: String, generation: String) =
    """
    CREATE MATERIALIZED VIEW $stage.mv__${tableName}_${generation}_to_topic_${definition.topicName}_${generation}
    TO $stage.topic_${definition.topicName}_${generation}
    AS SELECT
    (
        `changeset_timestamp`,
        `hashtag`,
        `user_id`,
        `country_iso_a3`,
        ${keyColumns(definition)}
        ${optionalAreaOrLengthColumnNames(definition)}
    )
    FROM $stage.${tableName}_${generation}
    WHERE
        changeset_timestamp > parseDateTimeBestEffort('$dateTime')
        AND
        (
            ${whereClause(definition)}
        )
    ;
    """.trimIndent().trimMargin()


@Suppress("LongMethod")
fun createTableDDL(definition: TopicDefinition, stage: String, generation: String) = """
        CREATE TABLE IF NOT EXISTS $stage.topic_${definition.topicName}_${generation}
        (
            `changeset_timestamp` DateTime,
            `hashtag`             String,
            `user_id`             Int32,
            `country_iso_a3`      Array(String),
            ${keyColumnDefinitions(definition)}
            ${optionalAreaOrLengthColumns(definition)}
        )
            ENGINE = MergeTree
            PRIMARY KEY( hashtag, changeset_timestamp)
        ;
    """.trimIndent()


private fun keyColumns(definition: TopicDefinition) = createFromKeys(definition, ::columnNames)
private fun keyColumnDefinitions(definition: TopicDefinition) = createFromKeys(definition, ::columnDefinitions)
private fun whereClause(definition: TopicDefinition) =
    createFromKeys(definition, ::whereClauseParts, "\n            OR\n")


private fun createFromKeys(definition: TopicDefinition, transform: (String) -> String, separator: String = ",\n") =
    definition
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
            ${key}_current  != '' OR ${key}_before != '' """


fun optionalAreaOrLengthColumns(definition: TopicDefinition) = if (definition.aggregationStrategy == LENGTH) {
    """,
            length          Int64,
            length_delta    Int64"""
} else
    ""

fun optionalAreaOrLengthColumnNames(definition: TopicDefinition) = if (definition.aggregationStrategy == LENGTH) {
    """,
        length,
        length_delta"""
} else
    ""


