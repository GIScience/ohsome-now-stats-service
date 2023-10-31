package org.heigit.ohsome.now.stats.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CountryHandlerTests {


    @Test
    fun `a null list of countries yields emtpy SQL string`() {

        val countryHandler = CountryHandler(null)

        assertThat(countryHandler.optionalFilterSQL)
            .isBlank()
    }


    @Test
    fun `an empty list of countries yields emtpy SQL string`() {

        val countryHandler = CountryHandler(emptyList())

        assertThat(countryHandler.optionalFilterSQL)
            .isBlank()
    }


    @Test
    fun `a list with 1 country yields a parameterized SQL string`() {

        val countryHandler = CountryHandler(listOf("UGA"))

        assertThat(countryHandler.optionalFilterSQL)
            .isEqualTo("AND hasAny(country_iso_a3, ['UGA'])")
    }


    @Test
    fun `a list with 2 countries yields a parameterized SQL string`() {

        val countryHandler = CountryHandler(listOf("UGA", "HUN"))

        assertThat(countryHandler.optionalFilterSQL)
            .isEqualTo("AND hasAny(country_iso_a3, ['UGA', 'HUN'])")
    }


}