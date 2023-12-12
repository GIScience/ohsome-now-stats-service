SELECT
   count(distinct changeset_id) as changesets,
   count(distinct user_id) as users,
   ifNull(sum(road_length_delta)/1000, 0) as roads,
   ifNull(sum(building_edit), 0) as buildings,
   count(map_feature_edit) as edits,
   toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as startdate,
   (toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime + INTERVAL :interval) as enddate
FROM "stats"
WHERE
   startsWith(hashtag, :hashtag)
 AND changeset_timestamp > parseDateTimeBestEffort(:startdate)
 AND changeset_timestamp < parseDateTimeBestEffort(:enddate)
 AND hasAny(country_iso_a3, ['BOL'])
GROUP BY
   startdate
ORDER BY startdate ASC
WITH FILL
         FROM toStartOfInterval(parseDateTimeBestEffort(:startdate), INTERVAL :interval)::DateTime
    TO (toStartOfInterval(parseDateTimeBestEffort(:enddate), INTERVAL :interval)::DateTime + INTERVAL :interval)
STEP INTERVAL :interval Interpolate (
    enddate as (
        if (
            startdate != parseDateTimeBestEffort('1970-01-01 00:00:00'), -- condition
            ((startdate + INTERVAL :interval) + INTERVAL :interval), 			 -- then
            (toStartOfInterval(parseDateTimeBestEffort(:startdate), INTERVAL :interval) + INTERVAL :interval) -- else
        )
 )
)
