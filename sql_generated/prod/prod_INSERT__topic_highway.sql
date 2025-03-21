-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-07T10:51:34.932453Z


INSERT into prod.topic_highway_3
SELECT
    changeset_timestamp,
    hashtags,
    user_id,
    country_iso_a3,
    
    tags['highway'] as  `highway_current`, 
    tags_before['highway'] as `highway_before`
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
        
        highway_current  != '' OR highway_before != '' 
    )
;