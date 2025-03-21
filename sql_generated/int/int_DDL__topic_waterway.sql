-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-21T14:21:01.049744Z


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
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
    PRIMARY KEY(has_hashtags, changeset_timestamp)
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
    
    `has_hashtags`
FROM int.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-21T18:21:00Z')
    AND
    (
        
        waterway_current  != '' OR waterway_before != '' 
    )
;