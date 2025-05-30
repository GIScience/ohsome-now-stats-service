-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-05-21T13:02:19.450180053Z


INSERT into int.topic_road_3
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
    int.all_stats_3
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2025-05-21T13:15:00Z')
    AND
    (
        highway_current  != '' OR highway_before != '' 
    )
;