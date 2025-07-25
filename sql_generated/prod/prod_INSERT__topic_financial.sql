-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-07-22T09:53:59.579894050Z


INSERT into prod.topic_financial_3
SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`,
    
    `has_hashtags`,
    `centroid`,
    `h3_r3`,
    `h3_r6`
FROM
    prod.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-07-22T13:53:59Z')
    AND
    (
        amenity_current  != '' OR amenity_before != '' 
    )
;