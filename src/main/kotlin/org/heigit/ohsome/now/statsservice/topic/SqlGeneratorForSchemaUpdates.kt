package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.AREA
import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.LENGTH


@Suppress("LongMethod")
fun createStatsTableDDL(stage: String, schemaVersion: String) = """
    -- main stats table definition
    CREATE TABLE IF NOT EXISTS $stage.all_stats_${schemaVersion}
    (
    `changeset_id`        Int64,
    `changeset_timestamp` DateTime('UTC'),

    `hashtags`            Array(String),

    `editor`              String,
    `user_id`             Int32,
    `osm_id`              String,
    `tags`                Map(String, String),
    `tags_before`         Map(String, String),

    -- areas
    `area`                Int64,
    `area_delta`          Int64,

    -- lengths
    `length`              Int64,
    `length_delta`        Int64,

    -- map feature stats
    `map_feature_edit`    Nullable(Int8), -- -1, 0, 1, NULL

    `has_hashtags`        Bool,
    `centroid`            Tuple(x Nullable(Float64), y Nullable(Float64)),
    `h3_r3`               Nullable(UInt64),
    `h3_r6`               Nullable(UInt64),
    `country_iso_a3`      Array(String),
    INDEX all_stats_${schemaVersion}_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
    INDEX all_stats_${schemaVersion}_skip_user_id_ix user_id TYPE bloom_filter(0.25) GRANULARITY 1
    )
    ENGINE = MergeTree
    PRIMARY KEY (
        has_hashtags,
        toStartOfDay(changeset_timestamp)
    )
    ORDER BY (
        has_hashtags,
        toStartOfDay(changeset_timestamp),
        geohashEncode(coalesce(centroid.x, 0), coalesce(centroid.y, 0), 2),
        changeset_timestamp
    )
    SETTINGS non_replicated_deduplication_window = 20000
    ;
   """.trimIndent().trimMargin()

fun createStatsTableProjections(stage: String, schemaVersion: String) = """
    -- for metadata endpoint
    ALTER TABLE ${stage}.all_stats_${schemaVersion} ADD PROJECTION timestamp_projection_${schemaVersion} (
    SELECT
        min(changeset_timestamp),
        max(changeset_timestamp)
    );
    
    ALTER TABLE ${stage}.all_stats_${schemaVersion} MATERIALIZE PROJECTION timestamp_projection_${schemaVersion};
    """.trimIndent().trimMargin()

fun createStatsTableMaterializedViewForHashtagAggregation(stage: String, schemaVersion: String) = """
    -- for hashtags endpoint
    CREATE TABLE IF NOT EXISTS ${stage}.hashtag_aggregation_${schemaVersion}
    (
        `hashtag`           String,
        `count`             UInt64
    )
        ENGINE = MergeTree
        PRIMARY KEY( count )
    ;
    """.trimIndent().trimMargin()

@Suppress("LongMethod")
fun createStatsMaterializedViewForHashtagAggregation(stage: String, schemaVersion: String) = """
-- we have to refresh the whole table periodically
-- because the count is *not* an incremental operation
CREATE MATERIALIZED VIEW ${stage}.mv__all_stats_${schemaVersion}_to_hashtag_aggregation_${schemaVersion}
REFRESH EVERY 1 MINUTE
TO ${stage}.hashtag_aggregation_${schemaVersion}
AS
SELECT
    arrayJoin(hashtags) AS hashtag,
    count(*) as count
FROM ${stage}.all_stats_${schemaVersion}
GROUP BY hashtag
;    
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
            `has_hashtags`        Bool,
            INDEX topic_${definition.topicName}_${topicSchemaVersion}_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
            INDEX topic_${definition.topicName}_${topicSchemaVersion}_skip_user_id_ix user_id TYPE bloom_filter(0.25) GRANULARITY 1
        )
            ENGINE = MergeTree
            PRIMARY KEY (
                         has_hashtags,
                         toStartOfDay(changeset_timestamp)
                        )
            ORDER BY (
                      has_hashtags,
                      toStartOfDay(changeset_timestamp),
                      geohashEncode(coalesce(centroid.x, 0), coalesce(centroid.y, 0), 2),
                      changeset_timestamp
                     )
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

