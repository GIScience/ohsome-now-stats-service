package org.heigit.ohsome.now.stats.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import org.heigit.ohsome.now.stats.topics

class TopicHandlerTests {
    private val topic: String = "place"

    @Test
    fun `check topic initialisation`() {
        val topicHandler = TopicHandler(topic)
        Assertions.assertEquals(topics["place"], topicHandler.definition)
        Assertions.assertEquals(topic, topicHandler.topic)
    }

    @Test
    fun `check valueLists construction one key`() {
        val topicHandler = TopicHandler(topic)
        val expectedValueLists =
            "['country', 'state', 'region', 'province', " +
                    "'district', 'county', 'municipality', " +
                    "'city', 'borough', 'suburb', 'quarter', " +
                    "'neighbourhood', 'town', 'village', 'hamlet', " +
                    "'isolated_dwelling'] as place_tags\n,"

        Assertions.assertEquals(expectedValueLists, topicHandler.valueLists)
    }

    @Test
    fun `check beforeCurrent construction one key`() {
        val topicHandler = TopicHandler(topic)
        val expectedBeforeAfter =
            "place_before in place_tags\n" +
                    "as before,\n" +
                    "place_current in place_tags\n" +
                    "as current,\n"

        Assertions.assertEquals(expectedBeforeAfter, topicHandler.beforeCurrent)
    }
}
