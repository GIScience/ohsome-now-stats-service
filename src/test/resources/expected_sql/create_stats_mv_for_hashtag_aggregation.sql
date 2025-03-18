-- needed for version 24.9.2.42 - obsolete in version 25.1.3.23
SET allow_experimental_refreshable_materialized_view=true;


-- we have to refresh the whole table periodically
-- because the count is *not* an incremental operation
CREATE MATERIALIZED VIEW int.mv__all_stats_7_to_hashtag_aggregation_7
REFRESH EVERY 1 MINUTE
TO int.hashtag_aggregation_7
AS
SELECT
    arrayJoin(hashtags) AS hashtag,
    count(*) as count
FROM int.all_stats_7
GROUP BY hashtag
;
