CREATE MATERIALIZED VIEW int.mv__stats_to_topic_amenity TO int.topic_amenity
AS
SELECT
    changeset_timestamp,
    hashtag,
    user_id,
    country_iso_a3,
    tags['amenity'] as amenity_current,
    tags_before['amenity'] as amenity_before
FROM int.stats
WHERE
        tags['amenity'] != '' OR tags_before['amenity'] != ''
;
