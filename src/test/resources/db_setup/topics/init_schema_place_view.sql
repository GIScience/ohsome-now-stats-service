DROP table IF EXISTS topic_place_2;

CREATE TABLE IF NOT EXISTS topic_place_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `place_current`       String,
    `place_before`        String

)
ENGINE = MergeTree
order by (`hashtag`, `changeset_timestamp`)
;