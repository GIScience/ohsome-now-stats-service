WITH
    ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling']
    as place_tags,

    place_before in place_tags as before,
    place_current in place_tags as current,

    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT
    ifNull(sum(edit), 0) as topic_result,
    user_id
FROM topic_place_2
WHERE
    user_id = :userId
    and startsWith(hashtag, 'hotosm-project-')
group by user_id