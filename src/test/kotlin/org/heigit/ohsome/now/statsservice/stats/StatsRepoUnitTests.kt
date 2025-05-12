package org.heigit.ohsome.now.statsservice.stats

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.file
import org.heigit.ohsome.now.statsservice.statsSchemaVersion
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.junit.jupiter.api.Test


class StatsRepoUnitTests {

    private val repo = StatsRepo()

    private val noHashtag = HashtagHandler("")
    private val fixedHashtag = HashtagHandler("hotmicrogrant")
    private val wildcardHashtag = HashtagHandler("hotmicrogrant*")

    private val allCountries = CountryHandler(emptyList())
    private val bolivia = CountryHandler(listOf("BOL"))

    @Test
    fun `can create stats SQL for all countries & no hashtag`() {

        val expected = file("stats_allcountries_no_hashtag")

        val sql = repo.statsFromTimeSpanSQL(noHashtag, allCountries)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun `can create stats SQL for all countries & non-wildcard hashtag`() {

        val expected = file("stats_allcountries_fixed_hashtag")

        val sql = repo.statsFromTimeSpanSQL(fixedHashtag, allCountries)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun `can create stats SQL for all countries & wildcard hashtag`() {

        val expected = file("stats_allcountries_wildcard_hashtag")

        val sql = repo.statsFromTimeSpanSQL(wildcardHashtag, allCountries)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }


    @Test
    fun `can create stats SQL by month, for 1 country & wildcard hashtag`() {

        val expected = file("stats_bymonth_1country_wildcard_hashtag")

        val sql = repo.statsFromTimeSpanIntervalSQL(wildcardHashtag, bolivia)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }


    @Test
    fun `can create stats SQL by month, for 1 country & no hashtag`() {

        val expected = file("stats_bymonth_1country_no_hashtag")

        val sql = repo.statsFromTimeSpanIntervalSQL(noHashtag, bolivia)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }


    @Test
    fun `can create stats SQL by country for wildcard hashtag`() {

        val expected = file("stats_bycountry_wildcard_hashtag")

        val sql = repo.statsFromTimeSpanCountrySQL(wildcardHashtag)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun `can create stats SQL by country without hashtag filter`() {

        val expected = file("stats_bycountry_no_hashtag")

        val sql = repo.statsFromTimeSpanCountrySQL(noHashtag)

        assertThat(sql)
            .contains("all_stats_$statsSchemaVersion")
            .isEqualToIgnoringWhitespace(expected)
    }


}
