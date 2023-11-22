WITH
    ['country', 'state', 'region', 'province', 'district', 'county', 'municipality', 'city', 'borough', 'suburb', 'quarter', 'neighbourhood', 'town', 'village', 'hamlet', 'isolated_dwelling'] as place_tags, place_current in place_tags as current, place_before in place_tags as before, if ((current = 0) AND (before = 0), NULL, current - before) as place_edit


SELECT sum(place_edit)

FROM topic_place
WHERE startsWith(hashtag, 'hotosm-project-')

  AND changeset_timestamp > parseDateTimeBestEffort('2009-04-21T22:02:04.000Z')
  AND changeset_timestamp < parseDateTimeBestEffort('2023-11-20T12:52:59.000Z')
;