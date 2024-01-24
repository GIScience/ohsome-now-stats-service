SELECT
    count(distinct changeset_id) as changesets,
    count(distinct user_id) as users,
    count(map_feature_edit) as edits,
    max(changeset_timestamp) as latest,
    country_iso_a3 as country
FROM "stats_2"
    ARRAY JOIN country_iso_a3
WHERE
    startsWith(hashtag, :hashtag)
  and changeset_timestamp > parseDateTimeBestEffort(:startDate)
  and changeset_timestamp < parseDateTimeBestEffort(:endDate)
GROUP BY
    country
ORDER BY country