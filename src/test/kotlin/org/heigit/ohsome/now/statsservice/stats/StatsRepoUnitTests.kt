package org.heigit.ohsome.now.statsservice.stats

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.file
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.junit.jupiter.api.Test


class StatsRepoUnitTests {

    private val repo = StatsRepo()

    private val fixedHashtag = HashtagHandler("hotmicrogrant")
    private val wildcardHashtag = HashtagHandler("hotmicrogrant*")

    private val allCountries = CountryHandler(emptyList())
    private val bolivia = CountryHandler(listOf("BOL"))



    @Test
    fun `can create stats SQL for all countries & non-wildcard hashtag`() {

        val expected = file("stats_allcountries_fixed_hashtag")

        val sql = repo.statsFromTimeSpanSQL(fixedHashtag, allCountries)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    @Test
    fun `can create stats SQL by month, for 1 country & wildcard hashtag`() {

        val expected = file("stats_bymonth_1country_wildcard_hashtag")

        val sql = repo.statsFromTimeSpanIntervalSQL(wildcardHashtag, bolivia)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    @Test
    fun `can create stats SQL by country for wildcard hashtag`() {

        val expected = file("stats_bycountry_wildcard_hashtag")

        val sql = repo.statsFromTimeSpanCountrySQL(wildcardHashtag)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


}
