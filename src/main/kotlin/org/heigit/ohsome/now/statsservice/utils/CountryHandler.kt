package org.heigit.ohsome.now.statsservice.utils

data class CountryHandler(val countries: List<String>) {
    val optionalFilterSQL: String

    init {
        val countryList = countries
            .filter(String::isNotBlank)
            .map { "'$it'" }

        optionalFilterSQL = if (countryList.isNotEmpty())
            "AND hasAny(country_iso_a3, $countryList)"
        else
            ""
    }
}