SELECT
   count(distinct changeset_id) as changesets,
   count(distinct user_id) as users,
   count(map_feature_edit) as edits,
   max(changeset_timestamp) as latest
FROM "stats_2"
WHERE
   equals(hashtag, :hashtag)
 and changeset_timestamp > parseDateTimeBestEffort(:startDate)
 and changeset_timestamp < parseDateTimeBestEffort(:endDate)

