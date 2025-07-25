-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-07-22T09:53:59.688441064Z


INSERT into prod.topic_power_3
SELECT
    `changeset_timestamp`,
    `hashtags`,
    `user_id`,
    `country_iso_a3`,
    
    tags['power'] as  `power_current`, 
    tags_before['power'] as `power_before`,
    
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
        power_current  != '' OR power_before != '' 
    )
;