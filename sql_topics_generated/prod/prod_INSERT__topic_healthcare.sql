-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T14:03:33.201218Z


INSERT into prod.topic_healthcare
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    
    tags['healthcare'] as  `healthcare_current`, 
    tags_before['healthcare'] as `healthcare_before`,

    tags['amenity'] as  `amenity_current`, 
    tags_before['amenity'] as `amenity_before`
     
FROM
    prod.stats;
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-12T18:03:33Z')
    AND
    (
        
        healthcare_current  != '' OR healthcare_before != '' 
        OR

        amenity_current  != '' OR amenity_before != '' 
    )