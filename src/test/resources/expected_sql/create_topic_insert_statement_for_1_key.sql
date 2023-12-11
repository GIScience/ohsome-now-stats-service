
INSERT into prod.topic_amenity
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    tags['amenity'] as amenity_current,
    tags_before['amenity'] as amenity_before
FROM
    prod.stats;
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-06-15 17:00:00')
    AND
    (
        amenity_current != '' OR amenity_before != ''
    )