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

/*
    @Test
    fun `can create SQL for topic 'place' by month, 1 country & wildcard hashtag`() {

        val expected = file("topic_place_bymonth_1country_wildcard_hashtag")

        val sql = repo.topicStatsFromTimeSpanIntervalSQL(wildcardHashtag, bolivia, placeTopic)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    @Test
    fun `can create SQL for topic 'healthcare', all countries & non-wildcard hashtag`() {

        val expected = file("topic_healthcare_allcountries_fixed_hashtag")

        val sql = repo.topicStatsFromTimeSpanSQL(fixedHashtag, allCountries, healthcareTopic)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    @Test
    fun `can create SQL for topic 'place' by country & wildcard hashtag`() {

        val expected = file("topic_place_bycountry_wildcard_hashtag")

        val sql = repo.topicStatsFromTimeSpanCountrySQL(wildcardHashtag, placeTopic)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    @Test
    fun `can create SQL for topic user endpoint`() {

        val expected = file("topic_place_by_userid")

        val sql = repo.topicForUserIdForHotOSMProjectSQL(placeTopic)
        assertThat(sql).isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


 */

}


