-- for hashtags endpoint
CREATE TABLE IF NOT EXISTS hashtag_aggregation_4
(
    `hashtag`           String,
    `count`             UInt64
)
    ENGINE = MergeTree
    PRIMARY KEY( count )
;
