-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-21T14:21:00.930105Z


CREATE TABLE IF NOT EXISTS int.topic_amenity_3
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

CREATE MATERIALIZED VIEW int.mv__all_stats_3_to_topic_amenity_3
TO int.topic_amenity_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    ,
    
    `has_hashtags`
FROM int.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-21T18:21:00Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )
;