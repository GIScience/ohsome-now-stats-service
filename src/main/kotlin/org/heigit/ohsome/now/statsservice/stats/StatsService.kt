package org.heigit.ohsome.now.statsservice.stats

import org.heigit.ohsome.now.statsservice.topic.TopicHandler
import org.heigit.ohsome.now.statsservice.topic.TopicRepo
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo

    @Autowired
    lateinit var topicRepo: TopicRepo


    @CacheEvict(value = ["statsForTimeSpan"], allEntries = true)
    @Scheduled(fixedRate = 20_000L)
    fun clearCache() {
    }


    @Cacheable("statsForTimeSpan")
    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?, countries: List<String>) =
        this.repo
            .getStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries))
            .toMutableMap()
            .addStatsForTimeSpanBuildingsAndRoads(handler(hashtag), startDate, endDate, handler(countries))
            .toStatsResult()


    @Suppress("LongMethod")
    fun MutableMap<String, Any>.addStatsForTimeSpanBuildingsAndRoads(
        hashtagHandler: HashtagHandler,
        startDate: Instant?,
        endDate: Instant?,
        countryHandler: CountryHandler
    ): Map<String, Any> {
        this["buildings"] = topicRepo.getTopicStatsForTimeSpan(
            hashtagHandler,
            startDate,
            endDate,
            countryHandler,
            TopicHandler("building")
        )["topic_result"].toString().toDouble().toLong()

        this["roads"] = topicRepo.getTopicStatsForTimeSpan(
            hashtagHandler,
            startDate,
            endDate,
            countryHandler,
            TopicHandler("highway")
        )["topic_result"].toString().toDouble()

        return this
    }

    fun getStatsForTimeSpanAggregate(hashtags: List<String>, startDate: Instant?, endDate: Instant?) = hashtags
        .map { getStatsForTimeSpanAggregate(it, startDate, endDate) }
        .reduce { m1, m2 -> m1 + m2 }


    private fun getStatsForTimeSpanAggregate(hashtag: String, startDate: Instant?, endDate: Instant?) = this.repo
        .getStatsForTimeSpanAggregate(handler(hashtag), startDate, endDate)
        .map {
            it.toMutableMap()
                .addStatsForTimeSpanBuildingsAndRoads(
                    handler(it["hashtag"].toString()), startDate, endDate, CountryHandler(
                        emptyList()
                    )
                )
        }
        .toMultipleStatsResult()


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

    @Suppress("LongMethod", "LongParameterList")
    fun List<Map<String, Any>>.addStatsForTimeSpanIntervalBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>
    ): List<Map<String, Any>> {
        var zipped: List<Map<String, Any>>
        val buildings = topicRepo.getTopicStatsForTimeSpanInterval(
            handler(hashtag),
            startDate,
            endDate,
            interval,
            handler(countries),
            TopicHandler("building")
        )
        zipped = this.zip(buildings) { a, b -> a.plus("buildings" to b["topic_result"].toString().toDouble().toLong()) }

        val roads = topicRepo.getTopicStatsForTimeSpanInterval(
            handler(hashtag),
            startDate,
            endDate,
            interval,
            handler(countries),
            TopicHandler("highway")
        )
        zipped = zipped.zip(roads) { a, b -> a.plus("roads" to b["topic_result"].toString().toDouble()) }

        return zipped

    }

    fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?) = this.repo
        .getStatsForTimeSpanCountry(handler(hashtag), startDate, endDate)
        .addStatsForTimeSpanCountriesBuildingsAndRoads(
            hashtag,
            startDate,
            endDate,
        )
        .toCountryStatsResult()

    @Suppress("LongMethod")
    fun List<Map<String, Any>>.addStatsForTimeSpanCountriesBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
    ): List<Map<String, Any>> {
        var zipped: List<Map<String, Any>>
        val buildings = topicRepo.getTopicStatsForTimeSpanCountry(
            handler(hashtag),
            startDate,
            endDate,
            TopicHandler("building")
        )
        zipped = this.map {
            it.plus("buildings" to buildings
                .find { iter -> iter["country"] == it["country"] }
                ?.get("topic_result").toString().nullToZero().toDouble().toLong())

        }

        val roads = topicRepo.getTopicStatsForTimeSpanCountry(
            handler(hashtag),
            startDate,
            endDate,
            TopicHandler("highway")
        )
        zipped = zipped.map {
            it.plus(
                "roads" to roads
                    .find { iter -> iter["country"] == it["country"] }
                    ?.get("topic_result").toString().nullToZero().toDouble()
            )
        }
        return zipped
    }

    fun String.nullToZero(): String {
        return if (this != "null") this else "0"
    }

    fun getMostUsedHashtags(startDate: Instant?, endDate: Instant?, limit: Int?) = this.repo
        .getMostUsedHashtags(startDate, endDate, limit)
        .toHashtagResult()


    fun getMetadata() = this.repo
        .getMetadata()
        .toMetadataResult()


    fun getUniqueHashtags() = this.repo
        .getUniqueHashtags()
        .toUniqueHashtagsResult()


    fun getStatsForUserIdForAllHotTMProjects(userId: String) = this.repo
        .getStatsForUserIdForAllHotTMProjects(userId)
        .addStatsForUserIdForAllHotTMProjectsBuildingsAndRoads(userId)
        .toUserResult()

    fun MutableMap<String, Any>.addStatsForUserIdForAllHotTMProjectsBuildingsAndRoads(userId: String): Map<String, Any> {
        this += topicRepo
            .getTopicForUserIdForAllHotTMProjects(userId, TopicHandler("building"))
            .topicResultToNamedResult("buildings")
        this += topicRepo
            .getTopicForUserIdForAllHotTMProjects(userId, TopicHandler("highway"))
            .topicResultToNamedResult("roads")
        println(this)
        return this
    }

    fun Map<String, Any>.topicResultToNamedResult(name: String): Map<String, Any> {
        val renamed = mutableMapOf(
            "$name" to this["topic_result"]!!,
            "${name}_created" to this["topic_result_created"]!!,
            "${name}_deleted" to this["topic_result_deleted"]!!,
            "${name}_modified" to this["topic_result_modified"]!!
        )
        if (name == "roads") {
            renamed += mapOf(
                "${name}_modified_longer" to this["topic_result_modified_more"]!!,
                "${name}_modified_shorter" to this["topic_result_modified_less"]!!
            )
        }
        return renamed
    }

    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)


}
