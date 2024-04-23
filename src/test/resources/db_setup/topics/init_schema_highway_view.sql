DROP TABLE IF EXISTS topic_highway_2;

CREATE TABLE IF NOT EXISTS topic_highway_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `highway_current`     String,
    `highway_before`      String,
    `length`              Int64,
    `length_delta`        Int64
)
ENGINE = MergeTree
ORDER BY (`hashtag`, `changeset_timestamp`);
