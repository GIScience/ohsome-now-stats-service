package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.AREA
import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.LENGTH


fun createStatsTableProjections(stage: String, schemaVersion: String) = """
    -- for metadata endpoint
    ALTER TABLE ${stage}.all_stats_${schemaVersion} ADD PROJECTION timestamp_projection_${schemaVersion} (
        SELECT
            changeset_timestamp
        ORDER BY
            changeset_timestamp
    );
    
    ALTER TABLE ${stage}.all_stats_${schemaVersion} MATERIALIZE PROJECTION timestamp_projection_${schemaVersion};
    """.trimIndent().trimMargin()


@Suppress("LongMethod", "LongParameterList")
fun createInsertStatement(
    definition: TopicDefinition,
    dateTime: String,
    stage: String,
    statsSchemaVersion: String,
    topicSchemaVersion: String
) = """
    INSERT into $stage.topic_${definition.topicName}_${topicSchemaVersion}
    SELECT
        changeset_timestamp,
        hashtags,
        user_id,
        country_iso_a3,
        ${keyColumns(definition)}
        ${optionalAreaOrLengthColumnNames(definition)}, 
        has_hashtags
    FROM
        $stage.all_stats_${statsSchemaVersion}
    WHERE
        changeset_timestamp <= parseDateTimeBestEffort('$dateTime')
        AND
        (
            ${whereClause(definition)}
        )
    ;
    """.trimIndent().trimMargin()


@Suppress("LongMethod", "LongParameterList")
fun createMvDdl(
    definition: TopicDefinition,
    dateTime: String,
    stage: String,
    statsSchemaVersion: String,
    topicSchemaVersion: String
) =
    """
    CREATE MATERIALIZED VIEW $stage.mv__all_stats_${statsSchemaVersion}_to_topic_${definition.topicName}_${topicSchemaVersion}
    TO $stage.topic_${definition.topicName}_${topicSchemaVersion}
    AS SELECT
        `changeset_timestamp`,
        `hashtags`,
        `user_id`,
        `country_iso_a3`,
        ${keyColumns(definition)}
        ${optionalAreaOrLengthColumnNames(definition)},
        
        `has_hashtags`
    FROM $stage.all_stats_${statsSchemaVersion}
    WHERE
        changeset_timestamp > parseDateTimeBestEffort('$dateTime')
        AND
        (
            ${whereClause(definition)}
        )
    ;
    """.trimIndent().trimMargin()


@Suppress("LongMethod")
fun createTableDDL(definition: TopicDefinition, stage: String, topicSchemaVersion: String) = """
        CREATE TABLE IF NOT EXISTS $stage.topic_${definition.topicName}_${topicSchemaVersion}
        (
            `changeset_timestamp` DateTime('UTC'),
            `hashtags`            Array(String),
            `user_id`             Int32,
            `country_iso_a3`      Array(String),
            ${keyColumnDefinitions(definition)}
            ${optionalAreaOrLengthColumns(definition)},
            `has_hashtags`        Bool
        )
            ENGINE = MergeTree
            PRIMARY KEY(has_hashtags, changeset_timestamp)
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
} else if (definition.aggregationStrategy == AREA) {
    """,
            area          Int64,
            area_delta    Int64"""
} else {
    ""
}

fun optionalAreaOrLengthColumnNames(definition: TopicDefinition) = if (definition.aggregationStrategy == LENGTH) {
    """,
        length,
        length_delta"""
} else if (definition.aggregationStrategy == AREA) {
    """,
            area,
            area_delta"""
} else {
    ""
}

