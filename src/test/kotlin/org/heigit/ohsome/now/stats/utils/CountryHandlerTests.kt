package org.heigit.ohsome.now.stats.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CountryHandlerTests {


    @Test
    fun `a null array of countries yields emtpy SQL string`() {

        val countryHandler = CountryHandler(null)

        assertThat(countryHandler.optionalFilterSQL)
            .isBlank()

        assertThat(countryHandler.isUsed)
            .isFalse()
    }


    @Test
    fun `an empty array of countries yields emtpy SQL string`() {

        val countryHandler = CountryHandler(emptyArray())

        assertThat(countryHandler.optionalFilterSQL)
            .isBlank()

        assertThat(countryHandler.isUsed)
            .isFalse()
    }

//TODO: isUsed()?

    @Test
    fun `an array with 1 country yields a parameterized SQL string`() {

        val countryHandler = CountryHandler(arrayOf("UGA"))

        assertThat(countryHandler.optionalFilterSQL)
            .isEqualTo("AND hasAny(country_iso_a3 ,['UGA'])")

        assertThat(countryHandler.isUsed)
            .isTrue()
    }


}