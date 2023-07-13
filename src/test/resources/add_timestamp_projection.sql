ALTER TABLE stats ADD PROJECTION user_id_projection (
    SELECT
        changeset_timestamp,
        hashtag,
        user_id
    ORDER BY
        toYYYYMMDD(changeset_timestamp),
        hashtag,
)
;