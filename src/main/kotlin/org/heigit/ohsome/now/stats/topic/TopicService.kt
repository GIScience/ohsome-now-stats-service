package org.heigit.ohsome.now.stats.topic

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

    // todo: check if topic is valid (contained as key in TopicDefinition)
    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpan(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        countries: List<String>,
        topics: List<String>
    ): Map<String, TopicResult> {
        val topicResults = mutableMapOf<String, TopicResult>()
        for (topic in topics) {
            topicResults[topic] = this.repo
                .getTopicStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries), TopicHandler(topic))
                .toTopicResult(topic)
        }
        return topicResults
    }

    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpanInterval(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>,
        topics: List<String>
    ): Map<String, List<TopicIntervalResult>> {
        val topicResults = mutableMapOf<String, List<TopicIntervalResult>>()
        for (topic in topics) {
            topicResults[topic] = this.repo
                .getTopicStatsForTimeSpanInterval(
                    handler(hashtag),
                    startDate,
                    endDate,
                    interval,
                    handler(countries),
                    TopicHandler(topic)
                ).toTopicIntervalResult(topic)

        }
        return topicResults
    }


    @Suppress("LongParameterList")
    fun getTopicStatsForTimeSpanCountry(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topics: List<String>
    ): Map<String, List<TopicCountryResult>> {
        val topicResults = mutableMapOf<String, List<TopicCountryResult>>()
        for (topic in topics) {
            topicResults[topic] = this.repo
                .getTopicStatsForTimeSpanCountry(
                    handler(hashtag),
                    startDate,
                    endDate,
                    TopicHandler(topic)
                ).toTopicCountryResult(topic)
        }
        return topicResults
    }


    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)


}
