package org.heigit.ohsome.now.stats.utils

class CountryHandler(countries: List<String>?) {
    val optionalFilterSQL: String

    init {
        if (!countries.isNullOrEmpty()) {
            val countryList = countries
                .toList()
                .map { "'$it'" }
            optionalFilterSQL = "AND hasAny(country_iso_a3, $countryList)"
        } else {
            optionalFilterSQL = ""
        }
    }
}