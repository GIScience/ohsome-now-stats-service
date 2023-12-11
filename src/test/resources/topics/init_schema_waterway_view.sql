DROP table IF EXISTS topic_waterway;

CREATE TABLE IF NOT EXISTS topic_waterway
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
order by (`hashtag`, `changeset_timestamp`)
;