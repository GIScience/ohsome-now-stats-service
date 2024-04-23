DROP TABLE IF EXISTS topic_amenity_2;

CREATE TABLE IF NOT EXISTS topic_amenity_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `amenity_current`       String,
    `amenity_before`        String
)
ENGINE = MergeTree
ORDER BY (`hashtag`, `changeset_timestamp`);
