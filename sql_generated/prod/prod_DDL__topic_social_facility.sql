-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-20T16:06:02.957570Z


CREATE TABLE IF NOT EXISTS prod.topic_social_facility_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `social_facility_current`      String, 
    `social_facility_before`       String,

    `amenity_current`      String, 
    `amenity_before`       String
    
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__stats_2_to_topic_social_facility_2
TO prod.topic_social_facility_2
AS SELECT
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['social_facility'] as  `social_facility_current`, 
    tags_before['social_facility'] as `social_facility_before`,

    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    
FROM prod.stats_2
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-20T20:06:02Z')
    AND
    (
        
        social_facility_current  != '' OR social_facility_before != '' 
        OR

        amenity_current  != '' OR amenity_before != '' 
    )
;