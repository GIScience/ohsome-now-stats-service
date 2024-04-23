DROP TABLE IF EXISTS topic_building_2;

CREATE TABLE topic_building_2
(
    `changeset_timestamp`   DateTime,
    `hashtag`               String,
    `user_id`               Int32,
    `country_iso_a3`        Array(String),
    `building_current`      String,
    `building_before`       String
)
ENGINE = MergeTree
ORDER BY (hashtag, changeset_timestamp);
