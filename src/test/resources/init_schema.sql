CREATE TABLE IF NOT EXISTS stats
(
    `deduplication_token` String,
    `changeset_id` Int64,
    `changeset_timestamp` DateTime,
    `hashtag` String,
    `user_id` Int32,
    `osm_id` String,

    -- map feature stats
    `map_feature_edit` Nullable(Int8), -- -1, 0, 1, NULL
    -- building stats
    `building_area` Int64,
    `building_area_delta` Int64,
    `building_edit` Nullable(Int8), -- -1, 0, 1, NULL
    -- road stats
    `road_length` Int64,
    `road_length_delta` Int64,
    `road_edit` Nullable(Int8), -- -1, 0, 1, NULL

    `country_iso_a3` Array(String),
)
ENGINE = MergeTree
PRIMARY KEY( hashtag, changeset_timestamp )
SETTINGS non_replicated_deduplication_window = 10000;
;