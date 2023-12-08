
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
    tags['healthcare'] != '' OR tags_before['healthcare'] != ''
    OR
    tags['amenity'] != '' OR tags_before['amenity'] != ''
