-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-12T13:21:03.318831Z


CREATE TABLE IF NOT EXISTS int.topic_place
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),
    
`place_current`      String, 
`place_before`       String
)
    ENGINE = MergeTree
    PRIMARY KEY( hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW int.mv__stats_to_topic_place TO int.topic_place
AS SELECT
(
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
    tags['place'] as  `place_current`, 
    tags_before['place'] as `place_before`
)
FROM int.stats
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-12T17:21:03Z')
    AND
    (
        
    place_current  != '' OR place_before != '' 
    )