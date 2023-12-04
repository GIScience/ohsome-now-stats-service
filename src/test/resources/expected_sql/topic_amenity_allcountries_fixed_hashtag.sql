 WITH
    amenity_before <> '' as before,
    amenity_current <> '' as current,
 
    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result

FROM topic_amenity
WHERE
    equals(hashtag, :hashtag) 
    and changeset_timestamp > parseDateTimeBestEffort(:startDate)
    and changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
