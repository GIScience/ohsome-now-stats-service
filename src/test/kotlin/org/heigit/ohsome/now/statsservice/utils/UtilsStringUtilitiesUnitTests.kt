package org.heigit.ohsome.now.statsservice.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UtilsStringUtilitiesUnitTests {

    @Test
    fun `translate ISO Period 'P1W' to Clickhouse interval '1 Week'`() {
        val result = getGroupbyInterval("P1W")
        assertThat(result).isEqualTo("1 WEEK")
    }

    @Test
    fun `translate ISO Period 'P1M' to Clickhouse interval '1 Month'`() {
        val result = getGroupbyInterval("P1M")
        assertThat(result).isEqualTo("1 MONTH")
    }

    @Test
    fun `translate ISO Period 'P1M' to Clickhouse interval '1 MINUTE'`() {
        val result = getGroupbyInterval("PT1M")
        assertThat(result).isEqualTo("1 MINUTE")
    }

    @Test
    fun `translate ISO Period 'P1Y' to Clickhouse interval '1 Year'`() {
        val result = getGroupbyInterval("P1Y")
        assertThat(result).isEqualTo("1 YEAR")
    }
}
