-- generated by org.heigit.ohsome.now.statsservice.TopicSqlGenerator at 2023-12-11T14:23:55.844856Z


    CREATE TABLE IF NOT EXISTS int.topic_healthcare
    (
        `changeset_timestamp` DateTime,
        `hashtag`             String,
        `user_id`             Int32,
        `country_iso_a3`      Array(String),
        
`healthcare_current`      String, 
`healthcare_before`       String,

`amenity_current`      String, 
`amenity_before`       String
    )
        ENGINE = MergeTree
        PRIMARY KEY( hashtag, changeset_timestamp)
    ;