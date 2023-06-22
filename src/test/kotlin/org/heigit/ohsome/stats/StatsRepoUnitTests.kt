package org.heigit.ohsome.stats

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test



class StatsRepoUnitTests{
        private lateinit var repo: StatsRepo
        @Test
        fun `translate ISO Period 'P1W' to Clickhouse interval '1 Week'`(){
            val result = this.repo.translateISOPeriodToClickhouseInterval("1W")
            assertThat(result).isEqualTo("1 Week")
        }
        @Test
        fun `translate ISO Period 'P1M' to Clickhouse interval '1 Month'`(){
            val result = this.repo.translateISOPeriodToClickhouseInterval("1M")
            assertThat(result).isEqualTo("1 Month")
        }
        @Test
        fun `translate ISO Period 'P1Y' to Clickhouse interval '1 Year'`(){
            val result = this.repo.translateISOPeriodToClickhouseInterval("1Y")
            assertThat(result).isEqualTo("1 Year")
        }
        @Test
        fun `translate ISO Period 'P1D' to Clickhouse interval '1 Day'`(){
            val result = this.repo.translateISOPeriodToClickhouseInterval("1D")
            assertThat(result).isEqualTo("1 Day")
        }
        @Test
        fun `translate ISO Period 'P1H' to Clickhouse interval '1 Hour'`(){
            val result = this.repo.translateISOPeriodToClickhouseInterval("1H")
            assertThat(result).isEqualTo("1 Hour")
        }
        @Test
        fun `translate ISO Period 'PT1M' to Clickhouse interval '1 Minute'`(){
            val result = this.repo.translateISOPeriodToClickhouseInterval("1M")
            assertThat(result).isEqualTo("1 Minute")
        }
}