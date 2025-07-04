WITH
    ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling']
    as place_tags,

    place_before in place_tags as before,
    place_current in place_tags as current,

    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result,
       ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
       ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
       ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted
FROM topic_user_place_3
WHERE
    has_hashtags = true
    AND arrayExists(hashtag -> startsWith(hashtag, :hashtag), hashtags)
    AND user_id = :userId
    AND changeset_timestamp > parseDateTimeBestEffort(:startDate)
    AND changeset_timestamp < parseDateTimeBestEffort(:endDate)
GROUP BY user_id