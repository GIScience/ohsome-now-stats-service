-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T14:02:12.898066Z


CREATE TABLE IF NOT EXISTS int.topic_waterway
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `waterway_current`      String, 
    `waterway_before`       String
    ,
    length          Int64,
    length_delta    Int64
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__stats_to_topic_waterway TO int.topic_waterway
AS SELECT
(
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['waterway'] as  `waterway_current`, 
    tags_before['waterway'] as `waterway_before`
    ,
        length,
        length_delta
)
FROM int.stats
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-12T18:02:12Z')
    AND
    (
        
        waterway_current  != '' OR waterway_before != '' 
    )