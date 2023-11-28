package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.models.toTopicCountryResult
import org.heigit.ohsome.now.stats.models.toTopicIntervalResult
import org.heigit.ohsome.now.stats.models.toTopicResult
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant


//TODO: add unit or integration tests

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


    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpanInterval(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>,
        topic: String
    ) = this.repo
        .getTopicStatsForTimeSpanInterval(handler(hashtag), startDate, endDate, interval, handler(countries), topic)
        .toTopicIntervalResult(topic)


    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpanCountry(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topic: String
    ) = this.repo
        .getTopicStatsForTimeSpanCountry(handler(hashtag), startDate, endDate, topic)
        .toTopicCountryResult(topic)


    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)


}
