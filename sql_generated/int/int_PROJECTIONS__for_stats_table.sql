-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2023-12-14T15:48:20.330530Z


ALTER TABLE int.stats_2 ADD PROJECTION timestamp_projection_2 (
    SELECT
        changeset_timestamp
    ORDER BY
        changeset_timestamp
);


ALTER TABLE int.stats_2 ADD PROJECTION user_id_projection_2 (
    SELECT
        user_id,
        building_area,
        road_length,
        hashtag
    ORDER BY
        user_id,
        hashtag
);