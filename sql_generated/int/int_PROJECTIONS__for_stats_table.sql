-- generated by org.heigit.ohsome.now.statsservice.SqlGenerator at 2025-03-21T14:21:00.878127Z


-- for metadata endpoint
ALTER TABLE int.all_stats_3 ADD PROJECTION timestamp_projection_3 (
    SELECT
        changeset_timestamp
    ORDER BY
        changeset_timestamp
);

ALTER TABLE int.all_stats_3 MATERIALIZE PROJECTION timestamp_projection_3;