-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-14T15:48:20.325136Z


INSERT into prod.topic_place_2
SELECT changeset_timestamp,
       hashtag,
       user_id,
       country_iso_a3,

       tags['place']        as `place_current`,
       tags_before['place'] as `place_before`

FROM prod.stats_2
WHERE changeset_timestamp <= parseDateTimeBestEffort('2023-12-14T12:15:43Z')
  AND (
    place_current != '' OR place_before != ''
    )