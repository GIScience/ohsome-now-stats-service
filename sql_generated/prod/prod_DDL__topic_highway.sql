-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.931760Z


CREATE TABLE IF NOT EXISTS prod.topic_highway_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `highway_current`      String, 
    `highway_before`       String
    ,
    length          Int64,
    length_delta    Int64,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
    PRIMARY KEY(has_hashtags, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__all_stats_3_to_topic_highway_3
TO prod.topic_highway_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['highway'] as  `highway_current`, 
    tags_before['highway'] as `highway_before`
    ,
    length,
    length_delta,
    
    `has_hashtags`
FROM prod.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-07T10:34:18Z')
    AND
    (
        
        highway_current  != '' OR highway_before != '' 
    )
;