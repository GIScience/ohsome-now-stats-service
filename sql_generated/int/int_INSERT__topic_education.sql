-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-05-09T13:22:48.982542015Z


INSERT into int.topic_education_3
SELECT
    changeset_timestamp,
    hashtags,
    user_id,
    country_iso_a3,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`,

    tags['building'] as  `building_current`, 
    tags_before['building'] as `building_before`
    , 
    has_hashtags,
    centroid
FROM
    int.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-05-05T00:00:00Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
        OR

        building_current  != '' OR building_before != '' 
    )
;