package org.heigit.ohsome.now.statsservice.stats

import org.heigit.ohsome.now.statsservice.topic.*
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

@Suppress("LargeClass")
@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo

    @Autowired
    lateinit var topicService: TopicService


    @CacheEvict(value = ["statsForTimeSpan"], allEntries = true)
    @Scheduled(cron = "\${ohsome.contribution.stats.service.cache.invalidation.cron}")
    fun clearCache() {
    }

    @Suppress("LongParameterList")
    @Cacheable("statsForTimeSpan", condition = "#hashtag=='hotosm-project-*' && #startDate==null && #endDate==null")
    fun getStatsForTimeSpan(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        countries: List<String>,
        topics: List<String>
    ) = addStatsTopicsForTimeSpan(
        HashtagHandler(hashtag), startDate, endDate, CountryHandler(countries), StatsTopicsHandler(topics)
    )
        .addTopicsForTimeSpan(hashtag, startDate, endDate, countries, topics.filter { !statsTopics.contains(it) })
        .toStatsResult()

    @Suppress("LongParameterList")
    private fun addStatsTopicsForTimeSpan(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler,
        statsTopicHandler: StatsTopicsHandler
    ): MutableMap<String, TopicResult> {
        if (statsTopicHandler.noStatsTopics) return mutableMapOf()
        val result =
            this.repo.getStatsForTimeSpan(hashtagHandler, startDate, endDate, countryHandler, statsTopicHandler)

        return statsTopicHandler.topics.associateWith { topic ->
            mapOf("topic_result" to result[topic]!!).toTopicResult(topic)
        }.toMutableMap()
    }

    @Suppress("LongParameterList")
    private fun MutableMap<String, TopicResult>.addTopicsForTimeSpan(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        countries: List<String>,
        topics: List<String>
    ) = this + topicService.getTopicStatsForTimeSpan(
        hashtag,
        startDate,
        endDate,
        countries,
        topics
    )


    fun getStatsForTimeSpanAggregate(hashtags: List<String>, startDate: Instant?, endDate: Instant?) = hashtags
        .map { getStatsForTimeSpanAggregate(it, startDate, endDate) }
        .reduce { m1, m2 -> m1 + m2 }


    private fun getStatsForTimeSpanAggregate(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?
    ): Map<String, StatsResult> {
        val statsResult = this.repo
            .getStatsForTimeSpanAggregate(handler(hashtag), startDate, endDate)

        val topicResults = this.topicService.getTopicStatsForTimeSpanAggregate(
            hashtag, startDate, endDate, listOf("building", "road")
        )

        return statsResult
            .mergeTopicIntoStatsAggregateResults(
                "buildings", topicResults.getOrDefault("building", emptyList())
            )
            .mergeTopicIntoStatsAggregateResults(
                "roads", topicResults.getOrDefault("road", emptyList())
            )
            .toMultipleStatsResult()
    }

    // this currently is only meant to be used with topics "roads" and "buildings" since these
    // are the only ones displayed on the missing maps partner pages
    @Suppress("CyclomaticComplexMethod")
    private fun List<MutableMap<String, Any>>.mergeTopicIntoStatsAggregateResults(
        resultObjectKey: String,
        topicResults: List<Pair<String, TopicResult>>
    ): List<MutableMap<String, Any>> {
        val defaultZero = getDefaultZero(resultObjectKey)

        if (topicResults.isEmpty()) {
            this.forEach { it[resultObjectKey] = defaultZero }
            return this
        }

        var topicIndex = 0
        this.forEach { result ->
            if (topicResults.getOrElse(topicIndex) { Pair(null, null) }.first == result["hashtag"]) {
                result[resultObjectKey] = getTopicResultInCorrectDataType(resultObjectKey, topicResults, topicIndex++)
            } else {
                result[resultObjectKey] = defaultZero
            }
        }
        return this
    }

    private fun getDefaultZero(resultObjectKey: String): Any =
        if (resultObjectKey == "roads") 0.toDouble() else 0.toLong()

    private fun getTopicResultInCorrectDataType(
        resultObjectKey: String,
        topicResults: List<Pair<String, TopicResult>>,
        topicIndex: Int
    ) = if (resultObjectKey == "roads")
        topicResults[topicIndex].second.value
    else
        topicResults[topicIndex].second.value.toLong()


    @Suppress("LongParameterList")
    fun getStatsForTimeSpanInterval(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>,
        topics: List<String>
    ) = addStatsTopicsForTimeSpanInterval(
        HashtagHandler(hashtag), startDate, endDate, interval, CountryHandler(countries), StatsTopicsHandler(topics)
    ).addTopicsForTimeSpanInterval(
        hashtag,
        startDate,
        endDate,
        interval,
        countries,
        topics.filter { !statsTopics.contains(it) },
    )

    @Suppress("LongParameterList")
    private fun addStatsTopicsForTimeSpanInterval(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countryHandler: CountryHandler,
        statsTopicHandler: StatsTopicsHandler
    ): StatsIntervalResultWithTopics {
        if (statsTopicHandler.noStatsTopics) return StatsIntervalResultWithTopics(null, null, mutableMapOf())

        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler, startDate, endDate, interval, countryHandler, statsTopicHandler
        )

        return StatsIntervalResultWithTopics(
            result["startdate"] as Array<LocalDateTime>,
            result["enddate"] as Array<LocalDateTime>,
            statsTopicHandler.topics.associateWith { topic ->
                mapOf("topic_result" to result[topic]!!).toTopicIntervalResult(topic)
            }.toMutableMap()
        )
    }

    @Suppress("LongParameterList")
    private fun StatsIntervalResultWithTopics.addTopicsForTimeSpanInterval(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>,
        topics: List<String>
    ): StatsIntervalResultWithTopics {
        val topicResults = topicService.getTopicStatsForTimeSpanInterval(
            hashtag, startDate, endDate, interval, countries, topics
        )
        // if no statsTopic was queried, we need the start and end-date once
        if (this.startDate.isNullOrEmpty()) {
            this.startDate = topicResults.values.first().startDate
            this.endDate = topicResults.values.first().endDate
        }

        removeDuplicateStartAndEndDates(topicResults)

        this.topics.putAll(topicResults)
        return this
    }

    private fun removeDuplicateStartAndEndDates(topicResults: Map<String, TopicIntervalResult>) {
        topicResults.values.forEach {
            it.startDate = null
            it.endDate = null
        }
    }

    private fun List<TopicCountryResult>.matchCountryValue(countryMap: Map<String, Any>) =
        (this.find { it.country == countryMap["country"] }
            ?.value ?: 0.0)

    fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?, topics: List<String>) =
        this.addStatsTopicsForTimeSpanCountry(handler(hashtag), startDate, endDate, StatsTopicsHandler(topics))
            .addTopicsForTimeSpanCountry(hashtag, startDate, endDate, topics.filter { !statsTopics.contains(it) })
            .toStatsTopicCountryResult()

    private fun addStatsTopicsForTimeSpanCountry(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        statsTopicHandler: StatsTopicsHandler
    ): MutableMap<String, List<TopicCountryResult>> {
        if (statsTopicHandler.noStatsTopics) return mutableMapOf()

        val result = repo.getStatsForTimeSpanCountry(hashtagHandler, startDate, endDate, statsTopicHandler)

        return statsTopicHandler.topics.associateWith { topic ->
            result.map { countryData ->
                topicCountryResult(
                    mapOf(
                        "topic_result" to countryData[topic]!!,
                        "country" to countryData["country"]!!
                    ),
                    topic
                )
            }
        }.toMutableMap()
    }

    private fun MutableMap<String, List<TopicCountryResult>>.addTopicsForTimeSpanCountry(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topics: List<String>
    ) = this + topicService.getTopicStatsForTimeSpanCountry(
        hashtag, startDate, endDate, topics
    )

    @Suppress("LongParameterList")
    fun getStatsByH3(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        topic: String,
        resolution: Int,
        countries: List<String>
    ): String {
        val statsTopicHandler = StatsTopicsHandler(listOf(topic))
        return if (!statsTopicHandler.noStatsTopics)
            this.repo
                .getStatsByH3(
                    handler(hashtag),
                    startDate,
                    endDate,
                    statsTopicHandler,
                    resolution,
                    CountryHandler(countries)
                )
        else this.topicService
            .getTopicsByH3(hashtag, startDate, endDate, topic, resolution, CountryHandler(countries))
    }

    fun getMostUsedHashtags(startDate: Instant?, endDate: Instant?, limit: Int?, countries: List<String>) = this.repo
        .getMostUsedHashtags(startDate, endDate, limit, handler(countries))
        .toHashtagResult()


    fun getMetadata() = this.repo
        .getMetadata()
        .toMetadataResult()


    fun getUniqueHashtags() = this.repo
        .getUniqueHashtags()
        .toUniqueHashtagsResult()

    @Suppress("LongParameterList")
    fun getStatsByUserId(
        userId: String,
        hashtag: String,
        topics: List<String>,
        startDate: Instant?,
        endDate: Instant?
    ) = this
        .addStatsTopicsByUserId(userId, HashtagHandler(hashtag), StatsTopicsHandler(topics), startDate, endDate)
        .addTopicsByUserId(userId, hashtag, topics.filter { !statsTopics.contains(it) }, startDate, endDate)
        .toUserResult(userId)

    @Suppress("LongParameterList")
    private fun addStatsTopicsByUserId(
        userId: String,
        hashtagHandler: HashtagHandler,
        statsTopicsHandler: StatsTopicsHandler,
        startDate: Instant?,
        endDate: Instant?
    ): MutableMap<String, TopicResult> {
        if (statsTopicsHandler.noStatsTopics) return mutableMapOf()

        val result = repo.getStatsByUserId(userId, hashtagHandler, statsTopicsHandler, startDate, endDate)
        return statsTopicsHandler.topics.associateWith { topic ->
            mapOf("topic_result" to result[topic]!!).toTopicResult(topic)
        }.toMutableMap()
    }

    @Suppress("LongParameterList")
    private fun MutableMap<String, TopicResult>.addTopicsByUserId(
        userId: String,
        hashtag: String,
        topics: List<String>,
        startDate: Instant?,
        endDate: Instant?
    ) = this + topicService.getTopicsByUserId(
        userId,
        topics,
        hashtag,
        startDate,
        endDate
    )

    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)
}
