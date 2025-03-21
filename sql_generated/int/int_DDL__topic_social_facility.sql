-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-21T14:21:01.045079Z


CREATE TABLE IF NOT EXISTS int.topic_social_facility_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `social_facility_current`      String, 
    `social_facility_before`       String,

    `amenity_current`      String, 
    `amenity_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
    PRIMARY KEY(has_hashtags, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__all_stats_3_to_topic_social_facility_3
TO int.topic_social_facility_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['social_facility'] as  `social_facility_current`, 
    tags_before['social_facility'] as `social_facility_before`,

    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    ,
    
    `has_hashtags`
FROM int.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-21T18:21:00Z')
    AND
    (
        
        social_facility_current  != '' OR social_facility_before != '' 
        OR

        amenity_current  != '' OR amenity_before != '' 
    )
;