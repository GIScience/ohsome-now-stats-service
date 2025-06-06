 WITH
    amenity_before <> '' as before,
    amenity_current <> '' as current,

    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result,
       ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
       ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
       ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted

FROM topic_amenity_3
WHERE
    has_hashtags = true
    AND has(hashtags, :hashtag)
    AND changeset_timestamp > parseDateTimeBestEffort(:startDate)
    AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
