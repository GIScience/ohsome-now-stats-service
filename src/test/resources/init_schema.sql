CREATE TABLE IF NOT EXISTS __stats_all_unnested
(
    `changeset_id` Int64,
    `changeset_timestamp`Int64,
    `hashtag` String,
    `user_id` Int32,

    `building_area` Nullable(Float64),
    `building_area_delta` Nullable(Float64),

    `road_length` Nullable(Float64),
    `road_length_delta` Nullable(Float64),
    `country_iso_a3` Array(String),
    `year` String
)
    ENGINE = MergeTree
    PRIMARY KEY
(
    hashtag
);

