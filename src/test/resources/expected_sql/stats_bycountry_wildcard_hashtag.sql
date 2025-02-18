SELECT
    count(distinct changeset_id) as changesets,
    count(distinct user_id) as users,
    count(map_feature_edit) as edits,
    max(changeset_timestamp) as latest,
    country_iso_a3 as country
FROM "all_stats_3"
    ARRAY JOIN country_iso_a3
WHERE
    arrayExists(hashtag -> startsWith(hashtag, :hashtag), hashtags)
    and changeset_timestamp > parseDateTimeBestEffort(:startDate)
    and changeset_timestamp < parseDateTimeBestEffort(:endDate)
GROUP BY
    country
ORDER BY country