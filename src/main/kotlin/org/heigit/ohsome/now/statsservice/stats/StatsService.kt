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

    @Deprecated(
        message = "remove together with Controller Funcs",
        replaceWith = ReplaceWith("getStatsForTimeSpan")
    )
    @Cacheable("statsForTimeSpan", condition = "#hashtag=='hotosm-project-*' && #startDate==null && #endDate==null")
    fun getStatsForTimeSpan(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        countries: List<String>,
    ) =
        this.repo
            .getStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries))
            .toMutableMap()
            .addStatsForTimeSpanBuildingsAndRoads(hashtag, startDate, endDate, countries)
            .toStatsResult()

    @Deprecated("Remove with controller")
    fun MutableMap<String, Any>.addStatsForTimeSpanBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        countries: List<String>
    ): Map<String, Any> {
        val topicResults = topicService.getTopicStatsForTimeSpan(
            hashtag,
            startDate,
            endDate,
            countries,
            listOf("building", "road")
        )
        this["buildings"] = topicResults["building"]!!.value.toLong()
        this["roads"] = topicResults["road"]!!.value

        return this
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
            if (topicResults[topicIndex].first == result["hashtag"]) {
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


    @Deprecated("remove with controller")
    @Suppress("LongParameterList")
    fun getStatsForTimeSpanInterval(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>
    ) = this.repo
        .getStatsForTimeSpanInterval(handler(hashtag), startDate, endDate, interval, handler(countries))
        .addStatsForTimeSpanIntervalBuildingsAndRoads(
            hashtag,
            startDate,
            endDate,
            interval,
            countries
        )
        .toIntervalStatsResult()

    @Deprecated("remove with controller")
    @Suppress("LongParameterList")
    private fun Map<String, Any>.addStatsForTimeSpanIntervalBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>
    ): Map<String, Any> {
        val topicResults = topicService.getTopicStatsForTimeSpanInterval(
            hashtag,
            startDate,
            endDate,
            interval,
            countries,
            listOf("building", "road")
        )
        return this.plus(
            mapOf(
                "buildings" to topicResults["building"]!!.value,
                "roads" to topicResults["road"]!!.value,
            )
        )
    }

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
                mapOf("topic_result" to result[topic]!!).toTopicIntervalResultMinusTopic(topic)
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

    @Deprecated("remove with controller")
    fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?) = this.repo
        .getStatsForTimeSpanCountry(handler(hashtag), startDate, endDate)
        .addStatsForTimeSpanCountriesBuildingsAndRoads(
            hashtag,
            startDate,
            endDate,
        )
        .toCountryStatsResult()

    @Deprecated("remove with controller")
    private fun List<Map<String, Any>>.addStatsForTimeSpanCountriesBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
    ): List<Map<String, Any>> {
        val topicResults = topicService.getTopicStatsForTimeSpanCountry(
            hashtag,
            startDate,
            endDate,
            listOf("building", "road")
        )
        val enrichedCountryList = this.map {
            it + ("buildings" to topicResults["building"]!!.matchCountryValue(it).toLong())
        }

        return enrichedCountryList.map {
            it + ("roads" to topicResults["road"]!!.matchCountryValue(it))
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


    fun getStatsByUserId(userId: String, hashtag: String, topics: List<String>) = this
        .addStatsTopicsByUserId(userId, HashtagHandler(hashtag), StatsTopicsHandler(topics))
        .addTopicsByUserId(userId, hashtag, topics.filter { !statsTopics.contains(it) })
        .toUserResult(userId)

    private fun addStatsTopicsByUserId(
        userId: String,
        hashtagHandler: HashtagHandler,
        statsTopicsHandler: StatsTopicsHandler
    ): MutableMap<String, TopicResultMinusTopic> {
        if (statsTopicsHandler.noStatsTopics) return mutableMapOf()

        val result = repo.getStatsByUserId(userId, hashtagHandler, statsTopicsHandler)
        return statsTopicsHandler.topics.associateWith { topic ->
            mapOf("topic_result" to result[topic]!!).toTopicResultMinusTopic(topic)
        }.toMutableMap()
    }

    private fun MutableMap<String, TopicResultMinusTopic>.addTopicsByUserId(
        userId: String,
        hashtag: String,
        topics: List<String>
    ) = this + topicService.getTopicsByUserId(
        userId,
        topics,
        hashtag
    )

    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)
}
