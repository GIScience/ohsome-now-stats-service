package org.heigit.ohsome.now.statsservice.topic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class TopicDefinitionTests {


    @Test
    fun `check topic aggregation strategy COUNT`() {

        val definition = TopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")), AggregationStrategy.COUNT)

        assertEquals(
            """
                ifNull(sum(edit), 0) as topic_result,
                ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
                ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
                ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted
            """.trimIndent(),
            definition.defineTopicResult().trimIndent()
        )
    }


    //TODO: enable again!!!!
    @Disabled
    @Test
    fun `check topic aggregation strategy LENTGH`() {
        // ! only mocking, not the real topic definition
        val definition = TopicDefinition("waterway", listOf(KeyOnlyMatcher("waterway")), AggregationStrategy.LENGTH)
        assertEquals(
            """
                ifNull(sum(multiIf(edit = 1, length,edit = 0, length_delta,edit = -1, - length + length_delta,0))/ 1000, 0) as topic_result
            
                ifNull(sum(if(edit = 1, length_delta, 0)) /1000, 0) as topic_created,
                ifNull(sum(if(edit = -1, length_delta, 0)) /1000, 0) as topic_deleted,
                ifNull(sum(if(edit = 0 and length_delta < 0, length_delta, 0)) /1000, 0) as topic_modified_shorter,
                ifNull(sum(if(edit = 0 and length_delta > 0, length_delta, 0)) /1000, 0) as topic_modified_longer,

            """.trimMargin(),
            definition.defineTopicResult()
        )
    }

    @Test
    fun `check topic aggregation strategy AREA`() {
        // ! only mocking, not the real topic definition
        val definition = TopicDefinition("landuse", listOf(KeyOnlyMatcher("landuse")), AggregationStrategy.AREA)
        assertEquals(
            "ifNull(sum(multiIf(edit = 1, area,edit = 0, area_delta,edit = -1, - area + area_delta,0))/ 1000000, 0) as topic_result",
            definition.defineTopicResult()
        )
    }
}
