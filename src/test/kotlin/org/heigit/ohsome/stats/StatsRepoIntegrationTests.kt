package org.heigit.ohsome.stats

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Disabled("testcontainers not yet working")
@Testcontainers
class StatsRepoIntegrationTests {


    @Container
    private val clickHouse: ClickHouseContainer = ClickHouseContainer()


    @Test
    fun `stats should return data from the db repo`() {

        assertTrue(clickHouse.isRunning)


    }


}


