-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T13:29:12.770179Z


INSERT into int.topic_waterway
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['waterway'] as  `waterway_current`, 
    tags_before['waterway'] as `waterway_before`
FROM
    int.stats;
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-12T17:29:12Z')
    AND
    (
        
        waterway_current  != '' OR waterway_before != '' 
    )