WITH
    ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags,
    place_before in place_tags as before,
    place_current in place_tags as current,

    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT
    ifNull(sum(edit), 0) as topic_result,
    country_iso_a3 as country
FROM topic_place_2

    ARRAY JOIN country_iso_a3
WHERE
    startsWith(hashtag, :hashtag)
  and changeset_timestamp > parseDateTimeBestEffort(:startDate)
  and changeset_timestamp < parseDateTimeBestEffort(:endDate)
GROUP BY
    country
ORDER BY
    country