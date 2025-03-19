-- for hashtags endpoint
CREATE TABLE IF NOT EXISTS hashtag_aggregation_3
(
    `hashtag`           String,
    `count`             UInt64
)
    ENGINE = MergeTree
    PRIMARY KEY( count )
;
