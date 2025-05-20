CREATE TABLE IF NOT EXISTS int.topic_healthcare_6
(
    `changeset_timestamp` DateTime('UTC'),
    `hashtags`            Array(String),
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    `healthcare_current`  String,
    `healthcare_before`   String,
    `amenity_current`     String,
    `amenity_before`      String,
    `has_hashtags`        Bool,
    `centroid`            Tuple(x Nullable(Float64), y Nullable(Float64)),
    `h3_r3`               Nullable(UInt64),
    `h3_r6`               Nullable(UInt64),
    INDEX topic_healthcare_6_skip_ht_ix hashtags TYPE set(0) GRANULARITY 1,
)
ENGINE = MergeTree
PRIMARY KEY (
             has_hashtags,
             toStartOfDay(changeset_timestamp)
            )
ORDER BY (
          has_hashtags,
          toStartOfDay(changeset_timestamp),
          geohashEncode(coalesce(centroid.x, 0), coalesce(centroid.y, 0), 2),
          changeset_timestamp
         )
;
