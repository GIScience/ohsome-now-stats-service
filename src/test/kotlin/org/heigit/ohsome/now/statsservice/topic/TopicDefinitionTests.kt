package org.heigit.ohsome.now.statsservice.topic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TopicDefinitionTests {


    @Test
    fun `check topic aggregation strategy COUNT`() {

        val definition = KeyOnlyTopicDefinition("amenity", "amenity")

        assertEquals("ifNull(sum(edit), 0)", definition.defineTopicResult())
    }


    @Test
    fun `check topic aggregation strategy LENTGH`() {
        // ! only mocking, not the real topic definition
        val definition = KeyOnlyTopicDefinition("waterway", "waterway", AggregationStrategy.LENGTH)
        assertEquals(
            "ifNull(intDiv(sum(multiIf(edit = 1, length,edit = 0, length_delta,edit = -1, - length + length_delta,0)), 1000), 0)",
            definition.defineTopicResult()
        )
    }
}
