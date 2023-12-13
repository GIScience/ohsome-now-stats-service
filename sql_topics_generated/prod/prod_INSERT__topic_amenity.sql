-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-13T16:10:29.968776Z


INSERT into prod.topic_amenity
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
     
FROM
    prod.stats
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-13T20:10:29Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )