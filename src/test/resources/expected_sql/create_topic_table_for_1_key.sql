CREATE TABLE IF NOT EXISTS int.topic_amenity_6
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `amenity_current`     String,
    `amenity_before`      String
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;
