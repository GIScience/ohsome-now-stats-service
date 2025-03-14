-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.742739Z


INSERT into int.topic_amenity_3
SELECT
    changeset_timestamp,
    hashtags,
    user_id,
    country_iso_a3,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
    , 
    has_hashtags
FROM
    int.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-03-07T10:34:18Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )
;