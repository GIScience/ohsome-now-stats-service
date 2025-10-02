SELECT
	groupArray(changeset::Float64)as changeset,
	groupArray(contributor::Float64)as contributor,
	groupArray(edit::Float64)as edit,
	groupArray(inner_startdate)as startdate,
	groupArray(inner_startdate + INTERVAL :interval)as enddate
FROM
(
    SELECT
       count(distinct changeset_id) as changeset,
       count(distinct user_id) as contributor,
       count(map_feature_edit) as edit,
       toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as inner_startdate
    FROM "all_stats_3"
    WHERE
        has_hashtags = true
        AND arrayExists(hashtag -> startsWith(hashtag, :hashtag), hashtags)
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
;
