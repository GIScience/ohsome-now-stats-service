DROP TABLE IF EXISTS topic_place_3;

CREATE TABLE topic_place_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `place_current`      String,
    `place_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
             PRIMARY KEY(has_hashtags, changeset_timestamp)
;

