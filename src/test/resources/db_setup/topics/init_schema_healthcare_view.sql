DROP TABLE IF EXISTS topic_healthcare_3;

CREATE TABLE IF NOT EXISTS topic_healthcare_3
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `healthcare_current`      String,
    `healthcare_before`       String,

    `amenity_current`      String,
    `amenity_before`       String
    ,
    `has_hashtags`        Bool
)
    ENGINE = MergeTree
             PRIMARY KEY(has_hashtags, changeset_timestamp)
;
