CREATE MATERIALIZED VIEW int.mv__stats_to_topic_healthcare TO int.topic_healthcare
AS
SELECT
    changeset_timestamp,
    hashtag,
    user_id,
    country_iso_a3,
    tags['healthcare'] as healthcare_current,
    tags_before['healthcare'] as healthcare_before,
    tags['amenity'] as amenity_current,
    tags_before['amenity'] as amenity_before
FROM int.stats
WHERE
        tags['healthcare'] != '' OR tags_before['healthcare'] != ''
        OR
        tags['amenity'] != '' OR tags_before['amenity'] != ''
;

