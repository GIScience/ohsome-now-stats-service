-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-14T15:48:20.324586Z


CREATE TABLE IF NOT EXISTS prod.topic_place_2
(
    `changeset_timestamp` DateTime,
    `hashtag`             String,
    `user_id`             Int32,
    `country_iso_a3`      Array(String),

    `place_current`       String,
    `place_before`        String

)
    ENGINE = MergeTree
        PRIMARY KEY (hashtag, changeset_timestamp)
;

CREATE MATERIALIZED VIEW prod.mv__stats_2_to_topic_place_2
    TO prod.topic_place_2
AS
SELECT `changeset_timestamp`,
       `hashtag`,
       `user_id`,
       `country_iso_a3`,

       tags['place']        as `place_current`,
       tags_before['place'] as `place_before`

FROM prod.stats_2
WHERE changeset_timestamp > parseDateTimeBestEffort('2023-12-14T12:15:43Z')
  AND (
    place_current != '' OR place_before != ''
    )
;