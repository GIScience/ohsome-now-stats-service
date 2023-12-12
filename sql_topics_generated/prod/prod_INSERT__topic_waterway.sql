-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T15:42:20.326438Z


INSERT into prod.topic_waterway
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['waterway'] as  `waterway_current`, 
    tags_before['waterway'] as `waterway_before`
    ,
    length,
    length_delta 
FROM
    prod.stats
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-12T19:42:20Z')
    AND
    (
        
        waterway_current  != '' OR waterway_before != '' 
    )