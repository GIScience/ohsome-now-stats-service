
INSERT into int.topic_healthcare
SELECT
    changeset_timestamp,
    user_id,
    hashtag,
    country_iso_a3,
    tags['healthcare'] as healthcare_current,
    tags_before['healthcare'] as healthcare_before,
    tags['amenity'] as amenity_current,
    tags_before['amenity'] as amenity_before
FROM
    int.stats;
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-06-15 17:00:00')
    AND
    (
        healthcare_current != '' OR healthcare_before != ''
        OR
        amenity_current != '' OR amenity_before != ''
    )

