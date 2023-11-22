CREATE TABLE IF NOT EXISTS topic_place
(
    changeset_timestamp DateTime,
    hashtag             String,
    user_id             Int32,
    place_current       String,
    place_before        String

) ENGINE = MergeTree
PRIMARY KEY( hashtag, changeset_timestamp )
