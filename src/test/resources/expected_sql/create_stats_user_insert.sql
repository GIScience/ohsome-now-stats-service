INSERT INTO int.all_stats_user_7
    SELECT
    `changeset_id`,
    `changeset_timestamp`,
    `hashtags`,
    `editor`,
    `user_id`,
    `osm_id`,
    `tags`,
    `tags_before`,
    `area`,
    `area_delta`,
    `length`,
    `length_delta`,
    `map_feature_edit`,
    `has_hashtags`,
    `centroid`,
    `h3_r3`,
    `h3_r6`,
    `country_iso_a3`
FROM int.all_stats_7
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-06-15 17:00:00')
;