package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.LENGTH



@Suppress("LongMethod")
fun createStatsTableProjections(stage: String, schemaVersion: String) = """

    ALTER TABLE ${stage}.stats_${schemaVersion} ADD PROJECTION timestamp_projection_${schemaVersion} (
        SELECT
            changeset_timestamp
        ORDER BY
            changeset_timestamp
    );
    
    
    ALTER TABLE ${stage}.stats_${schemaVersion} ADD PROJECTION user_id_projection_${schemaVersion} (
        SELECT
           user_id,
            building_area,
            road_length,
            hashtag
        ORDER BY
            user_id,
            hashtag
    );
    """.trimIndent().trimMargin()



@Suppress("LongMethod")
fun createInsertStatement(
    definition: TopicDefinition,
    dateTime: String,
    stage: String,
    schemaVersion: String
) = """
    INSERT into $stage.topic_${definition.topicName}_${schemaVersion}
    SELECT
        changeset_timestamp,
        user_id,
        hashtag,
        country_iso_a3,
        ${keyColumns(definition)}
        ${optionalAreaOrLengthColumnNames(definition)} 
    FROM
        $stage.stats_${schemaVersion}
    WHERE
        changeset_timestamp <= parseDateTimeBestEffort('$dateTime')
        AND
        (
            ${whereClause(definition)}
        )
    """.trimIndent().trimMargin()


@Suppress("LongMethod")
fun createMvDdl(definition: TopicDefinition, dateTime: String, stage: String, schemaVersion: String) =
    """
    CREATE MATERIALIZED VIEW $stage.mv__stats_${schemaVersion}_to_topic_${definition.topicName}_${schemaVersion}
    TO $stage.topic_${definition.topicName}_${schemaVersion}
    AS SELECT
        `changeset_timestamp`,
        `hashtag`,
        `user_id`,
        `country_iso_a3`,
        ${keyColumns(definition)}
        ${optionalAreaOrLengthColumnNames(definition)}
    FROM $stage.stats_${schemaVersion}
    WHERE
        changeset_timestamp > parseDateTimeBestEffort('$dateTime')
        AND
        (
            ${whereClause(definition)}
        )
    ;
    """.trimIndent().trimMargin()


@Suppress("LongMethod")
fun createTableDDL(definition: TopicDefinition, stage: String, schemaVersion: String) = """
        CREATE TABLE IF NOT EXISTS $stage.topic_${definition.topicName}_${schemaVersion}
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


