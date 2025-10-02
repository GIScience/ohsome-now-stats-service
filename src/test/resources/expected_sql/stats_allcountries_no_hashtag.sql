SELECT
   count(distinct changeset_id) as changeset,
   count(distinct user_id) as contributor,
   count(map_feature_edit) as edit
FROM "all_stats_3"
WHERE
   changeset_timestamp > parseDateTimeBestEffort(:startDate)
   AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
