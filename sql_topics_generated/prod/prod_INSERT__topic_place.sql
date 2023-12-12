-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T13:52:58.412781Z


INSERT into prod.topic_place
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['place'] as  `place_current`, 
    tags_before['place'] as `place_before`
FROM
    prod.stats;
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-12T17:52:58Z')
    AND
    (
        
        place_current  != '' OR place_before != '' 
    )