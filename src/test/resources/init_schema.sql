CREATE TABLE IF NOT EXISTS stats
(
    `changeset_id` Int64,
    `changeset_timestamp`Int64,
    `hashtag` String,
    `user_id` Int32,

    `building_area` Float64,
    `building_area_delta` Float64,

    `road_length` Float64,
    `road_length_delta` Float64,
    `country_iso_a3` Array(String),
    `year` String
)
    ENGINE = MergeTree
    PRIMARY KEY
(
    hashtag
);

