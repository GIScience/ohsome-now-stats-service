INSERT into int.topic_healthcare_6
SELECT changeset_timestamp,
       hashtag,
       user_id,
       country_iso_a3,
       tags['healthcare']        as `healthcare_current`,
       tags_before['healthcare'] as `healthcare_before`,
       tags['amenity']           as `amenity_current`,
       tags_before['amenity']    as `amenity_before`
FROM int.stats_7
WHERE changeset_timestamp <= parseDateTimeBestEffort('2023-06-15 17:00:00')
  AND (
    healthcare_current != '' OR healthcare_before != ''
        OR
    amenity_current != '' OR amenity_before != ''
    )

