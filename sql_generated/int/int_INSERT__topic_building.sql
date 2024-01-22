-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2024-01-22T16:25:41.151898969Z


INSERT into int.topic_building_2
SELECT
    changeset_timestamp,
    hashtag,
    user_id,
    country_iso_a3,
    
    tags['building'] as  `building_current`, 
    tags_before['building'] as `building_before`
     
FROM
    int.stats_2
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2024-01-22T20:25:41Z')
    AND
    (
        
        building_current  != '' OR building_before != '' 
    )