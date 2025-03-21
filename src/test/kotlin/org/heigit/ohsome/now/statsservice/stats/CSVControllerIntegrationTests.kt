package org.heigit.ohsome.now.statsservice.stats

import org.heigit.ohsome.now.statsservice.SpringTestWithClickhouse
import org.heigit.ohsome.now.statsservice.WithStatsData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container


@SpringTestWithClickhouse
@WithStatsData
class CSVControllerIntegrationTests {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = ClickHouseContainer("clickhouse/clickhouse-server:25.3.1.2703")
            .withEnv("CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT", "1")



        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }

    @Autowired
    lateinit var controller: CSVController



    @Test
    fun `csv can be directly fetched from the database`() {

        val result = this.controller.getCSVRows()
        result.forEach(::println)

        assertEquals(result[0], "1,3,2")
        assertEquals(result[1], "1,3,7")
        assertEquals(result[2], "2,3,2")
        assertEquals(result[3], "2,3,7")

        assertEquals(result.size, 4)

    }


}


