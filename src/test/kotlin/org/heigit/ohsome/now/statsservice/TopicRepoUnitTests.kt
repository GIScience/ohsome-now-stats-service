package org.heigit.ohsome.now.statsservice

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.topic.TopicHandler
import org.heigit.ohsome.now.statsservice.topic.TopicRepo
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.text.Charsets.UTF_8


class TopicRepoUnitTests {

    private val repo = TopicRepo()

    private val fixedHashtag = HashtagHandler("hotmicrogrant")
    private val wildcardHashtag = HashtagHandler("hotmicrogrant*")

    private val allCountries = CountryHandler(emptyList())
    private val bolivia = CountryHandler(listOf("BOL"))

    private val healthcareTopic = TopicHandler("healthcare")
    private val placeTopic = TopicHandler("place")
    private val amenityTopic = TopicHandler("amenity")


    @Test
    fun `can create SQL for topic 'amenity', all countries & non-wildcard hashtag`() {

        val expected = file("topic_amenity_allcountries_fixed_hashtag")

        val sql = repo.topicStatsFromTimeSpanSQL(fixedHashtag, allCountries, amenityTopic)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }




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



    private fun file(name: String) = File("src/test/resources/expected_sql/$name.sql")
        .readText(UTF_8)


}


