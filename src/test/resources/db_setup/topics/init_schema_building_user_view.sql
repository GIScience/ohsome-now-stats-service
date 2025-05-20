DROP TABLE IF EXISTS topic_user_building_3;


CREATE TABLE topic_user_building_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `building_current`      String,
    `building_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
             PRIMARY KEY(user_id, has_hashtags, changeset_timestamp)
;