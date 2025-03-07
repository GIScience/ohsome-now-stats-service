-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.806782Z


INSERT into int.topic_body_of_water_3
SELECT
    changeset_timestamp,
    hashtags,
    user_id,
    country_iso_a3,
    
    tags['water'] as  `water_current`, 
    tags_before['water'] as `water_before`
    , 
    has_hashtags
FROM
    int.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-03-07T10:34:18Z')
    AND
    (
        
        water_current  != '' OR water_before != '' 
    )
;