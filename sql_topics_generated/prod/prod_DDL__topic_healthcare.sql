-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-14T08:15:43.408180900Z


CREATE TABLE IF NOT EXISTS prod.topic_healthcare_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `healthcare_current`      String, 
    `healthcare_before`       String,

    `amenity_current`      String, 
    `amenity_before`       String
    
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__stats_2_to_topic_healthcare_2
TO prod.topic_healthcare_2
AS SELECT
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['healthcare'] as  `healthcare_current`, 
    tags_before['healthcare'] as `healthcare_before`,

    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    
FROM prod.stats_2
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-14T12:15:43Z')
    AND
    (
        
        healthcare_current  != '' OR healthcare_before != '' 
        OR

        amenity_current  != '' OR amenity_before != '' 
    )
;