SELECT
	groupArray(changesets)as changesets,
	groupArray(users)as users,
	groupArray(edits)as edits,
	groupArray(inner_startdate)as startdate,
	groupArray(inner_startdate + INTERVAL :interval)as enddate
FROM
(
    SELECT
       count(distinct changeset_id) as changesets,
       count(distinct user_id) as users,
       count(map_feature_edit) as edits,
       toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as inner_startdate
    FROM "stats_2"
    WHERE
       startsWith(hashtag, :hashtag)
     AND changeset_timestamp > parseDateTimeBestEffort(:startdate)
     AND changeset_timestamp < parseDateTimeBestEffort(:enddate)
     AND hasAny(country_iso_a3, ['BOL'])
    GROUP BY
       inner_startdate
    ORDER BY inner_startdate ASC
    WITH FILL
             FROM toStartOfInterval(parseDateTimeBestEffort(:startdate), INTERVAL :interval)::DateTime
        TO (toStartOfInterval(parseDateTimeBestEffort(:enddate), INTERVAL :interval)::DateTime + INTERVAL :interval)
    STEP INTERVAL :interval
)