CREATE TABLE IF NOT EXISTS prod.topic_healthcare_7
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `healthcare_current`  String,
    `healthcare_before`   String,
    `amenity_current`     String,
    `amenity_before`      String
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;
