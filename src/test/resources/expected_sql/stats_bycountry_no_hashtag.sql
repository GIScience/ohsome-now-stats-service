SELECT
    count(distinct changeset_id) as changeset,
    count(distinct user_id) as contributor,
    count(map_feature_edit) as edit,
    country_iso_a3 as country
FROM "all_stats_3"
    ARRAY JOIN country_iso_a3
WHERE
    changeset_timestamp > parseDateTimeBestEffort(:startDate)
    AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
GROUP BY
    country
ORDER BY country
;
