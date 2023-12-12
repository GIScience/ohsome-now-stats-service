-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T14:03:33.183839Z


INSERT into int.topic_amenity
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
     
FROM
    int.stats;
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-12T18:03:33Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )