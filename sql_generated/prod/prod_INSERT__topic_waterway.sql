-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.939624Z


INSERT into prod.topic_waterway_3
SELECT
    changeset_timestamp,
    hashtags,
    user_id,
    country_iso_a3,
    
    tags['waterway'] as  `waterway_current`, 
    tags_before['waterway'] as `waterway_before`
    ,
    length,
    length_delta, 
    has_hashtags
FROM
    prod.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-03-07T10:34:18Z')
    AND
    (
        
        waterway_current  != '' OR waterway_before != '' 
    )
;