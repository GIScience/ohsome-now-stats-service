-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-05-09T13:22:48.976234066Z


INSERT into int.topic_commercial_3
SELECT
    changeset_timestamp,
    hashtags,
    user_id,
    country_iso_a3,
    
    tags['shop'] as  `shop_current`, 
    tags_before['shop'] as `shop_before`
    , 
    has_hashtags,
    centroid
FROM
    int.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-05-05T00:00:00Z')
    AND
    (
        
        shop_current  != '' OR shop_before != '' 
    )
;