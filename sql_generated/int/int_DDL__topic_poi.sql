-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-21T14:21:01.039058Z


CREATE TABLE IF NOT EXISTS int.topic_poi_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `amenity_current`      String, 
    `amenity_before`       String,

    `shop_current`      String, 
    `shop_before`       String,

    `craft_current`      String, 
    `craft_before`       String,

    `office_current`      String, 
    `office_before`       String,

    `leisure_current`      String, 
    `leisure_before`       String,

    `aeroway_current`      String, 
    `aeroway_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
    PRIMARY KEY(has_hashtags, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__all_stats_3_to_topic_poi_3
TO int.topic_poi_3
AS SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`,

    tags['shop'] as  `shop_current`, 
    tags_before['shop'] as `shop_before`,

    tags['craft'] as  `craft_current`, 
    tags_before['craft'] as `craft_before`,

    tags['office'] as  `office_current`, 
    tags_before['office'] as `office_before`,

    tags['leisure'] as  `leisure_current`, 
    tags_before['leisure'] as `leisure_before`,

    tags['aeroway'] as  `aeroway_current`, 
    tags_before['aeroway'] as `aeroway_before`
    ,
    
    `has_hashtags`
FROM int.all_stats_3
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2025-03-21T18:21:00Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
        OR

        shop_current  != '' OR shop_before != '' 
        OR

        craft_current  != '' OR craft_before != '' 
        OR

        office_current  != '' OR office_before != '' 
        OR

        leisure_current  != '' OR leisure_before != '' 
        OR

        aeroway_current  != '' OR aeroway_before != '' 
    )
;