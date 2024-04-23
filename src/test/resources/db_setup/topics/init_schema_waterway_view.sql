DROP TABLE IF EXISTS topic_waterway_2;

CREATE TABLE IF NOT EXISTS topic_waterway_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `waterway_current`    String,
    `waterway_before`     String,
    `length`              Int64,
    `length_delta`        Int64
)
ENGINE = MergeTree
ORDER BY (`hashtag`, `changeset_timestamp`);