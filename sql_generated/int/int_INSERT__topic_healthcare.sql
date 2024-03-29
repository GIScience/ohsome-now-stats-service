-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-14T15:48:20.326256Z


INSERT into int.topic_healthcare_2
SELECT changeset_timestamp,
       hashtag,
       user_id,
       country_iso_a3,

       tags['healthcare']        as `healthcare_current`,
       tags_before['healthcare'] as `healthcare_before`,

       tags['amenity']           as `amenity_current`,
       tags_before['amenity']    as `amenity_before`

FROM int.stats_2
WHERE changeset_timestamp <= parseDateTimeBestEffort('2023-12-14T12:15:43Z')
  AND (
    healthcare_current != '' OR healthcare_before != ''
        OR
    amenity_current != '' OR amenity_before != ''
    )