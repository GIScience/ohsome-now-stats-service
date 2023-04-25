package org.heigit.ohsome.stats

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.testcontainers.containers.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest(webEnvironment = NONE)
@Testcontainers
class StatsRepoIntegrationTests {


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = ClickHouseContainer("clickhouse/clickhouse-server")


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) = registry.add("spring.datasource.url") { clickHouse.jdbcUrl }

    }


    @Autowired
    lateinit var repo: StatsRepo


    @Test
    @Sql(*["/init_schema.sql"])
    fun `stats should return data from the db repo`() {

        assertTrue(clickHouse.isRunning)

        val result = this.repo.getStats()
        println(result)
        assertEquals(6, result.size)
        assertEquals("{changesets=0, users=0, roads=null, buildings=0, edits=0, latest=1970-01-01T00:00}", result.toString())


//        this yields problems with clickhouse data types:

//        assertEquals(0, result["changesets"] )
//        assertEquals(0, result["users"] )
//        assertEquals(0.0, result["roads"] )
//        assertEquals(0, result["buildings"] )
//        assertEquals(0, result["edits"] )


    }


}


