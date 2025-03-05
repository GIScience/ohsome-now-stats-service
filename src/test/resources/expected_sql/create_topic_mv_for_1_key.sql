CREATE MATERIALIZED VIEW int.mv__stats_7_to_topic_amenity_6 TO int.topic_amenity_6
AS
SELECT `changeset_timestamp`,
       `hashtags`,
       `user_id`,
       `country_iso_a3`,

       tags['amenity']        as `amenity_current`,
       tags_before['amenity'] as `amenity_before`,

       `has_hashtags`

FROM int.stats_7
WHERE changeset_timestamp > parseDateTimeBestEffort('2023-06-15 17:00:00')
  AND (
    amenity_current != '' OR amenity_before != ''
    )
;


