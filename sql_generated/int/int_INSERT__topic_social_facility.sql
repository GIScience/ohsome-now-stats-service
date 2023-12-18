-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-15T12:52:21.247323663Z


INSERT into int.topic_social_facility_2
SELECT
    changeset_timestamp,
    hashtag,
    user_id,
    country_iso_a3,
    
    tags['social_facility'] as  `social_facility_current`, 
    tags_before['social_facility'] as `social_facility_before`
     
FROM
    int.stats_2
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-15T13:52:21Z')
    AND
    (
        
        social_facility_current  != '' OR social_facility_before != '' 
    )