
INSERT into int.topic_amenity
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    amenity,
    amenity_before
FROM
    int.stats;
WHERE
    amenity_current = ''  AND  amenity_before = ''

