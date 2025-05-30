DROP TABLE IF EXISTS topic_user_road_3;


CREATE TABLE topic_user_road_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `highway_current`      String,
    `highway_before`       String
    ,
    length          Int64,
    length_delta    Int64,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
             PRIMARY KEY(user_id, has_hashtags, changeset_timestamp)
;
