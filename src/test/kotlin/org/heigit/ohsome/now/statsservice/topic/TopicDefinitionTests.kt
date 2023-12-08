package org.heigit.ohsome.now.statsservice.topic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TopicDefinitionTests {


    @Test
    fun `check topic aggregation strategy COUNT`() {

        val definition = KeyOnlyTopicDefinition("amenity", "amenity")

        assertEquals("ifNull(sum(edit), 0)", definition.defineTopicResult())
    }



//TODO
//    @Test
//    fun `check topic aggregation strategy LENTGH`() {
//    }



}
