DROP TABLE IF EXISTS topic_waterway_3;


CREATE TABLE topic_waterway_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `waterway_current`      String,
    `waterway_before`       String
    ,
    length          Int64,
    length_delta    Int64,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
             PRIMARY KEY(has_hashtags, changeset_timestamp)
;
