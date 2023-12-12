CREATE MATERIALIZED VIEW prod.mv__stats_7_to_topic_amenity TO prod.topic_amenity
AS
SELECT
    changeset_timestamp,
    hashtag,
    user_id,
    country_iso_a3,
    tags['amenity'] as amenity_current,
    tags_before['amenity'] as amenity_before
FROM prod.stats_7
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-06-15 17:00:00')
    AND
    (
        amenity_current != '' OR amenity_before != ''
    )


