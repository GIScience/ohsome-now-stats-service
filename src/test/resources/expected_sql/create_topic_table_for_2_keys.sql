CREATE TABLE IF NOT EXISTS int.topic_healthcare_6
(
    `changeset_timestamp` DateTime('UTC'),
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
