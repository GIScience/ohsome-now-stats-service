package org.heigit.ohsome.now.stats

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
abstract class IntegrationTestWithClickhouse {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = ClickHouseContainer("clickhouse/clickhouse-server")


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }


}


