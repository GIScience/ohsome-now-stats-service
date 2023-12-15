-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-15T09:50:41.268662397Z


CREATE TABLE IF NOT EXISTS int.topic_shelter_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `social_facility_current`      String, 
    `social_facility_before`       String
    
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__stats_2_to_topic_shelter_2
TO int.topic_shelter_2
AS SELECT
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['social_facility'] as  `social_facility_current`, 
    tags_before['social_facility'] as `social_facility_before`
    
FROM int.stats_2
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-15T13:50:41Z')
    AND
    (
        
        social_facility_current  != '' OR social_facility_before != '' 
    )
;