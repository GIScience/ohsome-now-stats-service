-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-14T08:15:43.406238136Z


INSERT into prod.topic_amenity_2
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
     
FROM
    prod.stats_2
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-14T12:15:43Z')
    AND
    (
        
        amenity_current  != '' OR amenity_before != '' 
    )