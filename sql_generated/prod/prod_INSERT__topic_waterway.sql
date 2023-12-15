-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-14T15:48:20.329643Z


INSERT into prod.topic_waterway_2
SELECT changeset_timestamp,
       hashtag,
       user_id,
       country_iso_a3,

       tags['waterway']        as `waterway_current`,
       tags_before['waterway'] as `waterway_before`
        ,
       length,
       length_delta
FROM prod.stats_2
WHERE changeset_timestamp <= parseDateTimeBestEffort('2023-12-14T12:15:43Z')
  AND (
    waterway_current != '' OR waterway_before != ''
    )