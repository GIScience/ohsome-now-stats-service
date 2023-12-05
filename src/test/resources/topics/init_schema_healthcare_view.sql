DROP table IF EXISTS topic_healthcare;

create table topic_healthcare
(
    `changeset_timestamp` DateTime,
    `user_id` Int32,
    `hashtag` String,
    `country_iso_a3` Array(String),
    `healthcare_current` String,
    `healthcare_before` String,
    `amenity_current` String,
    `amenity_before` String
)
Engine = MergeTree
order by (`hashtag`, `changeset_timestamp`)
;