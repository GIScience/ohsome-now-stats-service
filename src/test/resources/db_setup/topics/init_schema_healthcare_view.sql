DROP TABLE IF EXISTS topic_healthcare_2;

CREATE TABLE topic_healthcare_2
(
    `changeset_timestamp`   DateTime,
    `user_id`               Int32,
    `hashtag`               String,
    `country_iso_a3`        Array(String),
    `healthcare_current`    String,
    `healthcare_before`     String,
    `amenity_current`       String,
    `amenity_before`        String
)
ENGINE = MergeTree
ORDER BY (`hashtag`, `changeset_timestamp`);
