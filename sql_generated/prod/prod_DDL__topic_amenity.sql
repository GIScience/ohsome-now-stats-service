-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.747791Z


CREATE TABLE IF NOT EXISTS prod.topic_amenity_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `amenity_current`      String, 
    `amenity_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
    PRIMARY KEY(has_hashtags, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__all_stats_3_to_topic_amenity_3
TO prod.topic_amenity_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    ,
    
    `has_hashtags`
FROM prod.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-07T10:34:18Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )
;