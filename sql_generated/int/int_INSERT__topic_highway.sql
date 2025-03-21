-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-21T14:21:01.042502Z


INSERT into int.topic_highway_3
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
    int.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-03-21T18:21:00Z')
    AND
    (
        
        highway_current  != '' OR highway_before != '' 
    )
;