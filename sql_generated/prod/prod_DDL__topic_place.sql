-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.912752Z


CREATE TABLE IF NOT EXISTS prod.topic_place_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `place_current`      String, 
    `place_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
    PRIMARY KEY(has_hashtags, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__all_stats_3_to_topic_place_3
TO prod.topic_place_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['place'] as  `place_current`, 
    tags_before['place'] as `place_before`
    ,
    
    `has_hashtags`
FROM prod.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-07T10:34:18Z')
    AND
    (
        
        place_current  != '' OR place_before != '' 
    )
;