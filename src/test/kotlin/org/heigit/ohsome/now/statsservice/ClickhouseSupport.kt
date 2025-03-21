package org.heigit.ohsome.now.statsservice

import org.testcontainers.clickhouse.ClickHouseContainer


fun createClickhouseContainer() = ClickHouseContainer("clickhouse/clickhouse-server:25.3.1.2703")
    .withUsername("default")
    .withPassword("")
    .withEnv("CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT", "1")
