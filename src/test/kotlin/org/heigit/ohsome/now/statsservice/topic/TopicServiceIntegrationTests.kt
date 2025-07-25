package org.heigit.ohsome.now.statsservice.topic

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.SpringTestWithClickhouse
import org.heigit.ohsome.now.statsservice.WithTopicData
import org.heigit.ohsome.now.statsservice.createClickhouseContainer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container


@SpringTestWithClickhouse
@WithTopicData
class TopicServiceIntegrationTests {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = createClickhouseContainer()


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }


    @Autowired
    lateinit var service: TopicService


    @Test
    fun `can get user topic stats for a given user id and a list of topics`() {
        val userId = 4362353
        val topic1 = "place"
        val topic2 = "healthcare"

        val topics = listOf(topic1, topic2)


        val result = this.service.getTopicsByUserId(userId.toString(), topics, "hotosm-project-*", null, null)

        val placeResult = result[topic1]!!
        assertThat(placeResult.value).isEqualTo(-1.0)
        println("--------------")
        println(placeResult)
        println("--------------")


        val healthcareResult = result[topic2]!!
        assertThat(healthcareResult.value).isEqualTo(0.0)
        println("--------------")
        println(healthcareResult)
        println("--------------")

    }


    @Test
    fun `can get user topic stats for a given user id and the 'amenity' topic`() {
        val userId = 3243541
        val topic = "place"

        val result =
            this.service.getTopicsByUserId(userId.toString(), listOf(topic), "hotosm-project-*", null, null)

        val amenityResult = result[topic]!!
        assertThat(amenityResult.value).isEqualTo(-1.0)
        println("--------------")
        println(amenityResult)
        println("--------------")
    }


}


