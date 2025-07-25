-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-07-22T09:53:59.719233235Z


INSERT into prod.topic_road_3
SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['highway'] as  `highway_current`, 
    tags_before['highway'] as `highway_before`,
    
    `length`,
    `length_delta`,
    `has_hashtags`,
    `centroid`,
    `h3_r3`,
    `h3_r6`
FROM
    prod.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-07-22T13:53:59Z')
    AND
    (
        highway_current  != '' OR highway_before != '' 
    )
;