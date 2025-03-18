-- for hashtags endpoint
CREATE TABLE IF NOT EXISTS int.hashtag_aggregation_7
(
    `hashtag`           String,
    `count`             UInt64
)
    ENGINE = MergeTree
    PRIMARY KEY( count )
;
