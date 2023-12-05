DROP table IF EXISTS topic_place;

CREATE TABLE IF NOT EXISTS topic_place
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `place_current`       String,
    `place_before`        String

) ENGINE =
    MergeTree
    PRIMARY
    KEY
(
    hashtag,
    changeset_timestamp
)
;