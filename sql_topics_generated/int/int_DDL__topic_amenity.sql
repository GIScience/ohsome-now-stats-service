-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-13T16:59:45.956652563Z


CREATE TABLE IF NOT EXISTS int.topic_amenity_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `amenity_current`      String, 
    `amenity_before`       String
    
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__stats_2_to_topic_amenity_2
TO int.topic_amenity_2
AS SELECT
(
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    
)
FROM int.stats_2
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-13T20:59:45Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )