INSERT into int.topic_user_amenity_6
SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,

    `amenity_current`,
    `amenity_before`,

    `has_hashtags`,
    `centroid`,
    `h3_r3`,
    `h3_r6`
FROM
    int.topic_amenity_6
;