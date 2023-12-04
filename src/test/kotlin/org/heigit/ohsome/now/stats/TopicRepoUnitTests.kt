package org.heigit.ohsome.now.stats

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.TopicHandler
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.text.Charsets.UTF_8


class TopicRepoUnitTests {

    private val repo = TopicRepo()

    private val fixedHashtag = HashtagHandler("hotmicrogrant")
    private val allCountries = CountryHandler(emptyList())
    private val healthcareTopic = TopicHandler("healthcare")


    @Test
    fun `can create SQL for topic 'healthcare', all countries & non-wildcard hashtag`() {

        val expected = file("topic_healthcare_allcountries_fixed_hashtag")

        val sql = repo.topicStatsFromTimeSpanSQL(fixedHashtag, allCountries, healthcareTopic)
        assertThat(sql)
            .isEqualToNormalizingPunctuationAndWhitespace(expected)
    }


    private fun file(name: String) = File("src/test/resources/expected_sql/$name.sql")
        .readText(UTF_8)


}


