-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-07-22T09:53:59.512055346Z


CREATE TABLE IF NOT EXISTS prod.topic_commercial_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `shop_current`      String, 
    `shop_before`       String,
    
    `has_hashtags`        Bool,
    `centroid`            Tuple(x Nullable(Float64), y Nullable(Float64)),
    `h3_r3`               Nullable(UInt64),
    `h3_r6`               Nullable(UInt64),
    INDEX topic_commercial_3_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
    INDEX topic_commercial_3_skip_cts_ix country_iso_a3 TYPE set(0) GRANULARITY 1
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

CREATE MATERIALIZED VIEW prod.mv__all_stats_3_to_topic_commercial_3
TO prod.topic_commercial_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['shop'] as  `shop_current`, 
    tags_before['shop'] as `shop_before`,
    
    `has_hashtags`,
    `centroid`,
    `h3_r3`,
    `h3_r6`
FROM prod.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-07-22T13:53:59Z')
    AND
    (
        shop_current  != '' OR shop_before != '' 
    )
;

-- USER MV --

CREATE TABLE IF NOT EXISTS prod.topic_user_commercial_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `shop_current`      String, 
    `shop_before`       String,
    
    `has_hashtags`        Bool,
    `centroid`            Tuple(x Nullable(Float64), y Nullable(Float64)),
    `h3_r3`               Nullable(UInt64),
    `h3_r6`               Nullable(UInt64),
INDEX topic_user_commercial_3_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
INDEX topic_user_commercial_3_skip_cts_ix country_iso_a3 TYPE set(0) GRANULARITY 1
)
ENGINE = MergeTree
PRIMARY KEY (
    user_id,
    has_hashtags,
    toStartOfDay(changeset_timestamp)
)
ORDER BY (
    user_id,
    has_hashtags,
    toStartOfDay(changeset_timestamp),
    geohashEncode(coalesce(centroid.x, 0), coalesce(centroid.y, 0), 2),
    changeset_timestamp
)
;

CREATE MATERIALIZED VIEW prod.mv__topic_commercial_3_to_topic_user_commercial_3
TO prod.topic_user_commercial_3
AS SELECT 
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    `shop_current`, 
    `shop_before`,
    
    `has_hashtags`,
    `centroid`,
    `h3_r3`,
    `h3_r6`
FROM prod.topic_commercial_3
;