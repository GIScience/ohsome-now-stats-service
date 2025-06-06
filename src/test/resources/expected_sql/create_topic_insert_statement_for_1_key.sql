INSERT into int.topic_amenity_6
SELECT `changeset_timestamp`,
       `hashtags`,
       `user_id`,
       `country_iso_a3`,
       tags['amenity']        as `amenity_current`,
       tags_before['amenity'] as `amenity_before`,
       `has_hashtags`,
       `centroid`,
       `h3_r3`,
       `h3_r6`
FROM int.all_stats_7
WHERE changeset_timestamp <= parseDateTimeBestEffort('2023-06-15 17:00:00')
  AND (
    amenity_current != '' OR amenity_before != ''
    )
;
