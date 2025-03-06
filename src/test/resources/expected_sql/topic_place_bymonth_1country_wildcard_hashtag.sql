SELECT
	groupArray(toFloat64(topic_result)) as topic_result,
	groupArray(toFloat64(topic_result_created)) as topic_result_created,
	groupArray(topic_result_modified) as topic_result_modified,
	groupArray(toFloat64(topic_result_deleted)) as topic_result_deleted,
	groupArray(inner_startdate) as startdate,
	groupArray(inner_startdate + INTERVAL :interval) as enddate
FROM
(
    WITH
        ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags,
        place_before in place_tags as before,
        place_current in place_tags as current,

        if ((current = 0) AND (before = 0), NULL, current - before) as edit

    SELECT ifNull(sum(edit), 0) as topic_result,
           ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
           ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
           ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted,
        toStartOfInterval(changeset_timestamp, INTERVAL :interval)::DateTime as inner_startdate

    FROM topic_place_3
    WHERE
        arrayExists(hashtag -> startsWith(hashtag, :hashtag), hashtags)
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