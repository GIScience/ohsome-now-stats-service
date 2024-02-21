-- for metadata endpoint
ALTER TABLE int.stats_7 ADD PROJECTION timestamp_projection_7 (
    SELECT
        changeset_timestamp
    ORDER BY
        changeset_timestamp
);

ALTER TABLE int.stats_7 MATERIALIZE PROJECTION timestamp_projection_7;


-- for hot-tm-user endpoint
ALTER TABLE int.stats_7 ADD PROJECTION user_id_projection_7 (
    SELECT
        user_id,
        hashtag,
        map_feature_edit,
        changeset_id
    ORDER BY
        user_id,
        hashtag
);

ALTER TABLE int.stats_7 MATERIALIZE PROJECTION user_id_projection_7;


-- for hashtags endpoint
ALTER TABLE int.stats_7 ADD PROJECTION hashtag_aggregation_projection_7
(
    SELECT
        hashtag,
        count(*)
    GROUP BY hashtag
);

ALTER TABLE int.stats_7 MATERIALIZE PROJECTION hashtag_aggregation_projection_7;
