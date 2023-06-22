package org.heigit.ohsome.stats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.mockito.Mockito


class StatsRepoUnitTests {

    private val repo = StatsRepo()


    @Test
    fun `translate ISO Period 'P1W' to Clickhouse interval '1 Week'`() {
        val result = this.repo.getGroupbyInterval("P1W")
        assertThat(result).isEqualTo("1 WEEK")
    }

    @Test
    fun `translate ISO Period 'P1M' to Clickhouse interval '1 Month'`() {
        val result = this.repo.getGroupbyInterval("P1M")
        assertThat(result).isEqualTo("1 MONTH")
    }

    @Test
    fun `translate ISO Period 'P1M' to Clickhouse interval '1 MINUTE'`() {
        val result = this.repo.getGroupbyInterval("PT1M")
        assertThat(result).isEqualTo("1 MINUTE")
    }

    @Test
    fun `translate ISO Period 'P1Y' to Clickhouse interval '1 Year'`() {
        val result = this.repo.getGroupbyInterval("P1Y")
        assertThat(result).isEqualTo("1 YEAR")
    }
}
