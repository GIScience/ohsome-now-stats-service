-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-14T15:48:20.322232Z


CREATE TABLE IF NOT EXISTS prod.topic_amenity_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `amenity_current`     String,
    `amenity_before`      String

)
    ENGINE = MergeTree
        PRIMARY KEY (hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__stats_2_to_topic_amenity_2
    TO prod.topic_amenity_2
AS
SELECT `changeset_timestamp`,
       `hashtag`,
       `user_id`,
       `country_iso_a3`,

       tags['amenity']        as `amenity_current`,
       tags_before['amenity'] as `amenity_before`

FROM prod.stats_2
WHERE changeset_timestamp > parseDateTimeBestEffort('2023-12-14T12:15:43Z')
  AND (
    amenity_current != '' OR amenity_before != ''
    )
;