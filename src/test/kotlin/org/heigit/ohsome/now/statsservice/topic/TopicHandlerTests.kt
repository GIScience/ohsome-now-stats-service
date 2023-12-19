package org.heigit.ohsome.now.statsservice.topic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TopicHandlerTests {

    private val topicOneKey: String = "place"
    private val topicTwoKeys: String = "healthcare"


    @Test
    fun `check topic initialisation`() {
        val topicHandler = TopicHandler(topicOneKey)
        assertEquals(topics["place"], topicHandler.definition)
        assertEquals(topicOneKey, topicHandler.topic)
    }


    @Test
    fun `check valueLists construction one key`() {
        val topicHandler = TopicHandler(topicOneKey)
        val expectedValueLists =
            "['country', 'state', 'region', 'province', " +
                    "'district', 'county', 'municipality', " +
                    "'city', 'borough', 'suburb', 'quarter', " +
                    "'neighbourhood', 'town', 'village', 'hamlet', " +
                    "'isolated_dwelling'] as place_tags,\n"

        assertEquals(expectedValueLists, topicHandler.valueLists)
    }


    @Test
    fun `check valueLists construction for mixed matchers`() {
        val topicHandler = TopicHandler(topicTwoKeys)
        val expectedValueLists =
                    "['doctors', 'dentist', 'clinic', 'hospital', 'pharmacy']" +
                    " as amenity_tags,\n"

        assertEquals(expectedValueLists, topicHandler.valueLists)
    }


    @Test
    fun `check beforeCurrent construction one key`() {
        val topicHandler = TopicHandler(topicOneKey)
        val expectedBeforeAfter =
            "place_before in place_tags\n" +
                    "as before,\n" +
                    "place_current in place_tags\n" +
                    "as current,\n"

        assertEquals(expectedBeforeAfter, topicHandler.beforeCurrent)
    }


    @Test
    fun `check beforeCurrent construction two keys`() {
        val topicHandler = TopicHandler(topicTwoKeys)

        val expectedBeforeAfter =
        " healthcare_before <> '' OR amenity_before in amenity_tags\n" +
        "as before,\n" +
        " healthcare_current <> '' OR amenity_current in amenity_tags\n" +
        "as current,\n"


        val actual = topicHandler.beforeCurrent

        assertEquals(expectedBeforeAfter, actual)
    }


    /*


    OR amenity_before in amenity_tags as before,


    OR amenity_current in amenity_tags as current,

     */



}
