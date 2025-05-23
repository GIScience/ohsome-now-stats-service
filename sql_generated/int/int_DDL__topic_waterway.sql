-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-05-09T13:22:49.055642496Z


CREATE TABLE IF NOT EXISTS int.topic_waterway_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `waterway_current`      String, 
    `waterway_before`       String
    ,
    length          Int64,
    length_delta    Int64,
    `has_hashtags`        Bool,
    `centroid`            Tuple(x Nullable(Float64), y Nullable(Float64)),
    INDEX topic_waterway_3_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
    INDEX topic_waterway_3_skip_user_id_ix user_id TYPE bloom_filter(0.25) GRANULARITY 1
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

CREATE MATERIALIZED VIEW int.mv__all_stats_3_to_topic_waterway_3
TO int.topic_waterway_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['waterway'] as  `waterway_current`, 
    tags_before['waterway'] as `waterway_before`
    ,
    length,
    length_delta,
    
    `has_hashtags`,
    `centroid`
FROM int.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-05-05T00:00:00Z')
    AND
    (
        
        waterway_current  != '' OR waterway_before != '' 
    )
;