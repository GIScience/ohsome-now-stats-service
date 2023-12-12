SELECT
   count(distinct changeset_id) as changesets,
   count(distinct user_id) as users,
   ifNull(sum(road_length_delta)/1000, 0) as roads,
   ifNull(sum(building_edit), 0) as buildings,
   count(map_feature_edit) as edits,
   max(changeset_timestamp) as latest
FROM "stats"
WHERE
   equals(hashtag, :hashtag)
 and changeset_timestamp > parseDateTimeBestEffort(:startDate)
 and changeset_timestamp < parseDateTimeBestEffort(:endDate)

