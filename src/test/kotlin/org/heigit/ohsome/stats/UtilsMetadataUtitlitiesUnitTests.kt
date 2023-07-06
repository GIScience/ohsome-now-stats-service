package org.heigit.ohsome.stats

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.stats.utils.echoRequestParameters
import org.junit.jupiter.api.Test
import java.time.Instant.now
import java.util.Map.entry


class UtilsMetadataUtitlitiesUnitTests {

    private val offsetDateTime = now()


    @Test
    fun `missing start and end dates lead to empty echo map`() {
        val result = echoRequestParameters(null, null)
        assertThat(result).isEmpty()
    }


    @Test
    fun `one of the dates present leads to only one entry in the echo map`() {

        val result1 = echoRequestParameters(startDate = offsetDateTime, endDate = null)
        assertThat(result1).containsExactly(entry("startdate", offsetDateTime))

        val result2 = echoRequestParameters(startDate = null, endDate = offsetDateTime)
        assertThat(result2).containsExactly(entry("enddate", offsetDateTime))
    }


    @Test
    fun `both dates present lead to two entry in the echo map`() {

        val result = echoRequestParameters(startDate = offsetDateTime, endDate = offsetDateTime)
        assertThat(result).containsExactly(entry("startdate", offsetDateTime), entry("enddate", offsetDateTime))
    }


}