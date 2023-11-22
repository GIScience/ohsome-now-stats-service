package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.models.toStatsResult
import org.heigit.ohsome.now.stats.models.toTopicResult
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class TopicService {

    @Autowired
    lateinit var repo: TopicRepo

    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpan(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        countries: List<String>,
        topic: String
    ) = this.repo
        .getTopicStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries), topic)
        .toTopicResult(topic)


    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)


}
