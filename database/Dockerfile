FROM clickhouse/clickhouse-server:23-alpine

COPY docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

# as long as ClickHouse initializes with every start of a container we need this entrypoint
COPY custom-entrypoint.sh /custom-entrypoint.sh

ENTRYPOINT ["/custom-entrypoint.sh"]