package org.heigit.ohsome.now.statsservice.utils

import org.heigit.ohsome.now.statsservice.topic.TopicHandler
import org.heigit.ohsome.now.statsservice.topic.topics
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TopicHandlerTests {
    private val topicOneKey: String = "place"
    private val topicTwoKeys: String = "healthcare"

    @Test
    fun `check topic initialisation`() {
        val topicHandler = TopicHandler(topicOneKey)
        Assertions.assertEquals(topics["place"], topicHandler.definition)
        Assertions.assertEquals(topicOneKey, topicHandler.topic)
    }

    @Test
    fun `check valueLists construction one key`() {
        val topicHandler = TopicHandler(topicOneKey)
        val expectedValueLists =
            "['country', 'state', 'region', 'province', " +
                    "'district', 'county', 'municipality', " +
                    "'city', 'borough', 'suburb', 'quarter', " +
                    "'neighbourhood', 'town', 'village', 'hamlet', " +
                    "'isolated_dwelling'] as place_tags\n,"

        Assertions.assertEquals(expectedValueLists, topicHandler.valueLists)
    }

    @Test
    fun `check valueLists construction two keys`() {
        val topicHandler = TopicHandler(topicTwoKeys)
        val expectedValueLists =
            "['doctors', 'clinic', 'midwife', 'nurse', 'center', 'health_post', 'hospital']" +
                    " as healthcare_tags\n," +
                    "['doctors', 'clinic', 'hospital', 'health_post']" +
                    " as amenity_tags\n,"

        Assertions.assertEquals(expectedValueLists, topicHandler.valueLists)
    }


    @Test
    fun `check beforeCurrent construction one key`() {
        val topicHandler = TopicHandler(topicOneKey)
        val expectedBeforeAfter =
            "place_before in place_tags\n" +
                    "as before,\n" +
                    "place_current in place_tags\n" +
                    "as current,\n"

        Assertions.assertEquals(expectedBeforeAfter, topicHandler.beforeCurrent)
    }

    @Test
    fun `check beforeCurrent construction two keys`() {
        val topicHandler = TopicHandler(topicTwoKeys)
        val expectedBeforeAfter =
            "healthcare_before in healthcare_tags\n" +
                    "OR amenity_before in amenity_tags\n" +
                    "as before,\n" +
                    "healthcare_current in healthcare_tags\n" +
                    "OR amenity_current in amenity_tags\n" +
                    "as current,\n"

        Assertions.assertEquals(expectedBeforeAfter, topicHandler.beforeCurrent)
    }


}
