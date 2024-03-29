-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-20T15:45:02.852772Z


CREATE TABLE IF NOT EXISTS int.topic_commercial_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
    `shop_current`      String, 
    `shop_before`       String
    
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__stats_2_to_topic_commercial_2
TO int.topic_commercial_2
AS SELECT
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['shop'] as  `shop_current`, 
    tags_before['shop'] as `shop_before`
    
FROM int.stats_2
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-20T19:45:02Z')
    AND
    (
        
        shop_current  != '' OR shop_before != '' 
    )
;