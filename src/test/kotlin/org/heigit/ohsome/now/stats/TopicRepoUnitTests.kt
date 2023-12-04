package org.heigit.ohsome.now.stats

import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.heigit.ohsome.now.stats.utils.TopicHandler
import org.junit.jupiter.api.Test


class TopicRepoUnitTests {

    private val repo = TopicRepo()

    val topic = "healthcare"

    private val allCountries = CountryHandler(emptyList())


    @Test
    fun `can create SQL for topic 'healthcare', all countries & non-wildcard hashtag`() {

        val expected = """
 WITH
    ['doctors', 'clinic', 'midwife', 'nurse', 'center', 'health_post', 'hospital'] as healthcare_tags,
    ['doctors', 'clinic', 'hospital', 'health_post'] as amenity_tags, 
            
    healthcare_before in healthcare_tags OR amenity_before in amenity_tags as before,
    healthcare_current in healthcare_tags OR amenity_current in amenity_tags as current,
 
    if ((current = 0) AND (before = 0), NULL, current - before) as edit

SELECT ifNull(sum(edit), 0) as topic_result

FROM topic_healthcare
WHERE
    equals(hashtag, :hashtag) 
    and changeset_timestamp > parseDateTimeBestEffort(:startDate)
    and changeset_timestamp < parseDateTimeBestEffort(:endDate)
;
""".trimIndent()

        val hashtagHandler = HashtagHandler("hotmicrogrant")

        val sql = repo.topicStatsFromTimeSpanSQL(hashtagHandler, allCountries, TopicHandler(topic))

        println(sql)

        assertThat(sql).isEqualToNormalizingPunctuationAndWhitespace(expected)

    }




}


