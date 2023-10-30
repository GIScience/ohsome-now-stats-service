package org.heigit.ohsome.now.stats.utils

class CountryHandler(var countries: Array<String>?) {
    var optionalFilterSQL = ""
    var isUsed = false

    init {
        isUsed = !countries.isNullOrEmpty() // notice the !
        if (isUsed) {
            val countries = countries
                ?.toList()
                ?.map { "'$it'" }
            optionalFilterSQL = "AND hasAny(country_iso_a3 ,$countries)"
        }
    }
}