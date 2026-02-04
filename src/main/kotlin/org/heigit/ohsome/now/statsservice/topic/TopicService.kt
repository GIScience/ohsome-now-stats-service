package org.heigit.ohsome.now.statsservice.topic

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.heigit.ohsome.now.statsservice.utils.UserHandler
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
        topics: List<String>,
        userId: String
    ): Map<String, TopicResult> = runBlocking {
        val deferredResults = topics.map { topic ->
            async(IO) {
                repo.getTopicStatsForTimeSpan(
                    handler(hashtag),
                    startDate,
                    endDate,
                    handler(countries),
                    TopicHandler(topic),
                    UserHandler(userId)
                ).toTopicResult(topic)
            }.let { topic to it }
        }

        deferredResults.associate { (topic, deferred) -> topic to deferred.await() }
    }

    fun getTopicStatsForTimeSpanAggregate(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topics: List<String>,
    ): Map<String, List<Pair<String, TopicResult>>> {
        val topicResults = mutableMapOf<String, List<Pair<String, TopicResult>>>()
        for (topic in topics) {
            topicResults[topic] = this.repo
                .getTopicStatsForTimeSpanAggregate(handler(hashtag), startDate, endDate, TopicHandler(topic))
                .map { Pair(it["hashtag"].toString(), it.toTopicResult(topic)) }
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
        topics: List<String>,
        userId: String
    ): Map<String, TopicIntervalResult> = runBlocking {
        val deferredResults = topics.map { topic ->
            async(IO) {
                repo.getTopicStatsForTimeSpanInterval(
                    handler(hashtag),
                    startDate,
                    endDate,
                    interval,
                    handler(countries),
                    TopicHandler(topic),
                    UserHandler(userId)
                ).toTopicIntervalResult(topic)
            }.let { topic to it }
        }

        deferredResults.associate { (topic, deferred) -> topic to deferred.await() }
    }


    fun getTopicStatsForTimeSpanCountry(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topics: List<String>
    ): Map<String, List<TopicCountryResult>> = runBlocking {
        val deferredResults = topics.map { topic ->
            async(IO) {
                repo.getTopicStatsForTimeSpanCountry(
                    handler(hashtag),
                    startDate,
                    endDate,
                    TopicHandler(topic)
                ).toTopicCountryResult(topic)
            }.let { topic to it }
        }

        deferredResults.associate { (topic, deferred) -> topic to deferred.await() }
    }


    @Suppress("LongParameterList")
    fun getTopicsByH3(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topic: String,
        resolution: Int,
        countryHandler: CountryHandler
    ) =
        this.repo
            .getTopicsByH3(handler(hashtag), startDate, endDate, TopicHandler(topic), resolution, countryHandler)


    fun getTopicDefinitions(topics: List<String>?): Map<String, String> {
        val topicDefinitionMap = buildTopicDefinitionMap()
        return if (topics.isNullOrEmpty()) {
            topicDefinitionMap
        } else {
            topicDefinitionMap.filterKeys(topics::contains)
        }
    }

    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)
}
