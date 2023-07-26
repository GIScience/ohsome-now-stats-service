ALTER TABLE stats ADD PROJECTION timestamp_projection (
    SELECT
        changeset_timestamp
    ORDER BY
        changeset_timestamp
)
;