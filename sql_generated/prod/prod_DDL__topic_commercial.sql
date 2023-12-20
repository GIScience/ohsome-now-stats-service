-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-20T16:06:02.945968Z


CREATE TABLE IF NOT EXISTS prod.topic_commercial_2
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

CREATE MATERIALIZED VIEW prod.mv__stats_2_to_topic_commercial_2
TO prod.topic_commercial_2
AS SELECT
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['shop'] as  `shop_current`, 
    tags_before['shop'] as `shop_before`
    
FROM prod.stats_2
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-20T20:06:02Z')
    AND
    (
        
        shop_current  != '' OR shop_before != '' 
    )
;