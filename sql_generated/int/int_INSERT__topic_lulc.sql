-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-20T16:06:02.952373Z


INSERT into int.topic_lulc_2
SELECT
    changeset_timestamp,
    hashtag,
    user_id,
    country_iso_a3,
    
    tags['landuse'] as  `landuse_current`, 
    tags_before['landuse'] as `landuse_before`,

    tags['natural'] as  `natural_current`, 
    tags_before['natural'] as `natural_before`,

    tags['waterway'] as  `waterway_current`, 
    tags_before['waterway'] as `waterway_before`
    ,
        area,
        area_delta 
FROM
    int.stats_2
WHERE
    changeset_timestamp <= parseDateTimeBestEffort('2023-12-20T20:06:02Z')
    AND
    (
        
        landuse_current  != '' OR landuse_before != '' 
        OR

        natural_current  != '' OR natural_before != '' 
        OR

        waterway_current  != '' OR waterway_before != '' 
    )