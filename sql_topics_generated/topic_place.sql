-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-11T14:23:55.841778Z


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