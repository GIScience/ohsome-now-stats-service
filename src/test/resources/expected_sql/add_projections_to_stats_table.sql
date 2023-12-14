ALTER TABLE int.stats_7 ADD PROJECTION timestamp_projection_7 (
    SELECT
    changeset_timestamp
    ORDER BY
        changeset_timestamp
)
;


ALTER TABLE int.stats_7 ADD PROJECTION user_id_projection_7 (
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
