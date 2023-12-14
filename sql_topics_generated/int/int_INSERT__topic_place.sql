-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-14T14:14:37.864402Z


INSERT into int.topic_place_2
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['place'] as  `place_current`, 
    tags_before['place'] as `place_before`
     
FROM
    int.stats_2
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-14T18:14:37Z')
    AND
    (
        
        place_current  != '' OR place_before != '' 
    )