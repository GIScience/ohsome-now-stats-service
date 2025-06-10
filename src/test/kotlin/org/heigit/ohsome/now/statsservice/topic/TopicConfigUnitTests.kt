package org.heigit.ohsome.now.statsservice.topic

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test


class TopicConfigUnitTests {

    private val validNames = listOf("education", "poi", "amenity", "edit")
    private val mixedNames = listOf("education", "poi", "amenity", "kartoffelsupp")


    @Test
    fun areTopicsValid() {

        assertTrue(areTopicsValid(validNames))
        assertFalse(areTopicsValid(mixedNames))

    }


}