-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-11T15:46:42.557648Z


    CREATE TABLE IF NOT EXISTS int.topic_amenity
    (
        `changeset_timestamp` DateTime,
        `hashtag`             String,
        `user_id`             Int32,
        `country_iso_a3`      Array(String),
        
`amenity_current`      String, 
`amenity_before`       String
    )
        ENGINE = MergeTree
        PRIMARY KEY( hashtag, changeset_timestamp)
    ;

CREATE MATERIALIZED VIEW int.mv__stats_to_topic_amenity TO int.topic_amenity
AS SELECT
(
    `changeset_timestamp`,
    `hashtag`,
    `user_id`,
    `country_iso_a3`,
    
tags['amenity'] as  `amenity_current`, 
tags_before['amenity'] as `amenity_before`
)
FROM int.stats
WHERE
    changeset_timestamp > parseDateTimeBestEffort('2023-12-11T19:46:42Z')
    AND
    (
        
amenity_current  != '' OR amenity_before != '' 
    )