SELECT
   count(distinct changeset_id) as changesets,
   count(distinct user_id) as users,
   count(map_feature_edit) as edits,
   max(changeset_timestamp) as latest
FROM "all_stats_3"
WHERE
   changeset_timestamp > parseDateTimeBestEffort(:startDate)
   AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
