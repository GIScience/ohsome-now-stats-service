ALTER TABLE stats ADD PROJECTION user_id_projection (
    SELECT
        user_id,
        building_area,
        road_length,
        hashtag
    ORDER BY
        user_id,
        hashtag
)
;