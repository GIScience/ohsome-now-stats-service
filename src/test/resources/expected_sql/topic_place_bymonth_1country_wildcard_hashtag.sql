WITH
    ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags,
    place_before in place_tags as before,
    place_current in place_tags as current,

    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result,
       ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
       ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
       ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted,
    toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as startdate,
    (toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime + INTERVAL :interval) as enddate

FROM topic_place_2
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
