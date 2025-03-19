TRUNCATE all_stats_3;

INSERT INTO all_stats_3 (
  "changeset_id",
  "changeset_timestamp",
  "hashtags",
  "editor",
  "user_id",
  "osm_id",
  "map_feature_edit",

  "has_hashtags",
  "country_iso_a3"
)
VALUES
  (37627942, toDateTime(1240437600), ['&'], 'StreetComplete', 2186388, 'way/3698274', NULL,                 true, ['FIN']),
  (37627942, toDateTime(1457186420), ['&'], 'StreetComplete', 2186388, 'node/7805319', 1,                   true, ['FIN']),
  (37627942, toDateTime(1457186420), ['hotosm-project-1'], 'StreetComplete',  2186388, 'way/21875648', 0,   true, []),
  (110552980, toDateTime(1630486233), ['&gid'], 'IdEditor', 219908, 'way/64192853', NULL,                   true, ['BEL']),
  (110552120, toDateTime(1513644723), ['&gid'], 'IdEditor', 219908, 'way/64192853', NULL,                   true, ['BEL']),
  (110552980, toDateTime(1630486233), ['&gid'], 'IdEditor', 219908, 'node/102547876', 0,                    true, ['BEL']),
  (110552980, toDateTime(1630486233), ['&gid'], 'IdEditor', 219908, 'node/154769328', NULL,                 true, ['BEL']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/281438526', NULL,                true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'node/759279192', -1,                 true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/542187403', NULL,                true, ['HUN', 'BEL']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'node/824619875', 1,                  true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/935724631', NULL,                true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'node/6017532492', -1,                true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/8726149325', NULL,               true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'node/8207649533', 1,                 true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/34269857412', 0,                 true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'node/95827410352', -1,               true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/684210394875', NULL,             true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'node/5428710439801', 0,              true, ['HUN']),
  (114743552, toDateTime(1639054888), ['&group'], 'IdEditor', 552187, 'way/71098765432109', NULL,           true, ['HUN']),
  (1147435529, toDateTime(1639054888), ['&groupExtra'], 'IdEditor', 552187, 'way/684210394875', NULL,       true, ['HUN']),
  (1147435529, toDateTime(1639054888), ['&groupExtra'], 'IdEditor', 552187, 'node/5428710439801', 0,        true, ['HUN']),
  (1147435529, toDateTime(1639054888), ['&groupExtra'], 'IdEditor', 552187, 'way/71098765432109', NULL,     true, ['HUN']),
  (54746053, toDateTime(1513644723), ['&uganda'], 'JOSM', 6791950, 'node/123456789012345', NULL,            true, ['UGA','XYZ']),
  (114743252, toDateTime(1513644723), ['&test'], 'JOSM', 123456, 'way/3698274', -1,                         true, ['DE']),
  (114743252, toDateTime(1513644723), ['&test'], 'JOSM', 123456, 'node/7805319', NULL,                      true, ['DE']),
  (114743252, toDateTime(1513644723), ['&test'], 'JOSM', 123456, 'way/21875648', NULL,                      true, []),
  (114743252, toDateTime(1513644723), ['&test'], 'JOSM', 123456, 'way/64192853', NULL,                      true, ['DE']),
  (114743252, toDateTime(1688042925), ['&test'], 'JOSM', 123456, 'node/102547876', 0,                       true, ['DE']),
  (114743252, toDateTime(1688042925), ['hotosm-project-12312'], 'JOSM', 2186388, 'node/102547876', 1,       true, ['DE']),
  (114743252, toDateTime(1688042925), ['hotosm-project-12312'], 'JOSM', 2186388, 'node/102547876', 1,       true, ['DE']),
  (114743252, toDateTime(1688042925), [], 'JOSM', 2186388, 'node/102547876', 1,       false, ['DE'])
;
