-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-11T17:11:00.469316Z


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
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-11T21:11:00Z')
    AND
    (
        
    waterway_current  != '' OR waterway_before != '' 
    )