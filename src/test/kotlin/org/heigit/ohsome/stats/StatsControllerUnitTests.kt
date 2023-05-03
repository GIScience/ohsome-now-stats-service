package org.heigit.ohsome.stats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime.now
import java.util.Map.entry


class StatsControllerUnitTests {

    private val controller = StatsController()
    private val offsetDateTime = now()


    @Test
    fun `missing start and end dates lead to empty echo map`() {

        val result = this.controller.echoRequestParameters(null, null)
        assertThat(result).isEmpty()
    }


    @Test
    fun `one of the dates present leads to only one entry in the echo map`() {

        val result1 = this.controller.echoRequestParameters(startDate = offsetDateTime, endDate = null)
        assertThat(result1).containsExactly(entry("startdate", offsetDateTime))

        val result2 = this.controller.echoRequestParameters(startDate = null, endDate = offsetDateTime)
        assertThat(result2).containsExactly(entry("enddate", offsetDateTime))
    }


    @Test
    fun `both dates present lead to two entry in the echo map`() {

        val result = this.controller.echoRequestParameters(startDate = offsetDateTime, endDate = offsetDateTime)
        assertThat(result).containsExactly(entry("startdate", offsetDateTime), entry("enddate", offsetDateTime))
    }


}