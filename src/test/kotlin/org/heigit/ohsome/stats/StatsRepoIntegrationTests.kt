package org.heigit.ohsome.stats

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
class StatsRepoIntegrationTests {


    @Container
    private val clickHouse: ClickHouseContainer = ClickHouseContainer("clickhouse/clickhouse-server")


    @Test
    fun `stats should return data from the db repo`() {

        assertTrue(clickHouse.isRunning)

    }


}


