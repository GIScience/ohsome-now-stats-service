WITH

    ['doctors', 'dentist', 'clinic', 'hospital', 'pharmacy'] as amenity_tags,


    healthcare_before <> ''
    OR amenity_before in amenity_tags as before,

    healthcare_current <> ''
    OR amenity_current in amenity_tags as current,

    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result,
       ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
       ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
       ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted

FROM topic_healthcare_3
WHERE
    has_hashtags = true
    AND arrayExists(hashtag -> equals(hashtag, :hashtag), hashtags)
    AND changeset_timestamp > parseDateTimeBestEffort(:startDate)
    AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
