 WITH
    ['doctors', 'clinic', 'midwife', 'nurse', 'center', 'health_post', 'hospital'] as healthcare_tags,
    ['doctors', 'clinic', 'hospital', 'health_post'] as amenity_tags, 
            
    healthcare_before in healthcare_tags OR amenity_before in amenity_tags as before,
    healthcare_current in healthcare_tags OR amenity_current in amenity_tags as current,
 
    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result

FROM topic_healthcare_2
WHERE
    equals(hashtag, :hashtag) 
    and changeset_timestamp > parseDateTimeBestEffort(:startDate)
    and changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
