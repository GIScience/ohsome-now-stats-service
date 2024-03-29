SELECT
	groupArray(changesets)as changesets,
	groupArray(users)as users,
	groupArray(edits)as edits,
	groupArray(startdate)as startdate,
	groupArray(enddate)as enddate
FROM
(
    SELECT
       count(distinct changeset_id) as changesets,
       count(distinct user_id) as users,
       count(map_feature_edit) as edits,
       toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as startdate,
       (toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime + INTERVAL :interval) as enddate
    FROM "stats_2"
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
)