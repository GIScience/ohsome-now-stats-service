-- for metadata endpoint
ALTER TABLE int.all_stats_7 ADD PROJECTION timestamp_projection_7 (
    SELECT
        changeset_timestamp
    ORDER BY
        changeset_timestamp
);

ALTER TABLE int.all_stats_7 MATERIALIZE PROJECTION timestamp_projection_7;

