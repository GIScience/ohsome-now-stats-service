-- main stats table definition
CREATE TABLE IF NOT EXISTS int.all_stats_7
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
    INDEX all_stats_7_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
    INDEX all_stats_7_skip_user_id_ix user_id TYPE bloom_filter(0.25) GRANULARITY 1
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
