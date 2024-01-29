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


@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo

    @Autowired
    lateinit var topicService: TopicService

    @CacheEvict(value = ["statsForTimeSpan"], allEntries = true)
    @Scheduled(fixedRate = 20_000L)
    fun clearCache() {
    }


    @Cacheable("statsForTimeSpan")
    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?, countries: List<String>) =
        this.repo
            .getStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries))
            .toMutableMap()
            .addStatsForTimeSpanBuildingsAndRoads(hashtag, startDate, endDate, countries)
            .toStatsResult()


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
            listOf("building", "highway")
        )
        this["buildings"] = topicResults["building"]!!.value.toLong()
        this["roads"] = topicResults["highway"]!!.value

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
                    it["hashtag"].toString(), startDate, endDate, emptyList()
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

    @Suppress("LongParameterList")
    private fun List<Map<String, Any>>.addStatsForTimeSpanIntervalBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
        interval: String,
        countries: List<String>
    ): List<Map<String, Any>> {
        val topicResults = topicService.getTopicStatsForTimeSpanInterval(
            hashtag,
            startDate,
            endDate,
            interval,
            countries,
            listOf("building", "highway")
        )
        val zipped =
            this.zip(topicResults["building"]!!) { a, b -> a + ("buildings" to b.value.toLong()) }

        return zipped.zip(topicResults["highway"]!!) { a, b -> a + ("roads" to b.value) }
    }

    fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?) = this.repo
        .getStatsForTimeSpanCountry(handler(hashtag), startDate, endDate)
        .addStatsForTimeSpanCountriesBuildingsAndRoads(
            hashtag,
            startDate,
            endDate,
        )
        .toCountryStatsResult()

    // todo: write mocked repo unit test
    private fun List<Map<String, Any>>.addStatsForTimeSpanCountriesBuildingsAndRoads(
        hashtag: String,
        startDate: Instant?,
        endDate: Instant?,
    ): List<Map<String, Any>> {
        val topicResults = topicService.getTopicStatsForTimeSpanCountry(
            hashtag,
            startDate,
            endDate,
            listOf("building", "highway")
        )
        val enrichedCountryList = this.map {
            it + ("buildings" to topicResults["building"]!!.matchCountryValue(it).toLong())
        }

        return enrichedCountryList.map {
            it + ("roads" to topicResults["highway"]!!.matchCountryValue(it))
        }
    }

    private fun List<TopicCountryResult>.matchCountryValue(countryMap: Map<String, Any>) =
        (this.find { it.country == countryMap["country"] }
            ?.value ?: 0.0)


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

    private fun MutableMap<String, Any>.addStatsForUserIdForAllHotTMProjectsBuildingsAndRoads(userId: String): Map<String, Any> {
        val topicResults = topicService.getTopicsForUserIdForAllHotTMProjects(
            userId,
            listOf("building", "highway"),
            "hotosm-project-*"
        )
        this += topicResults["building"]!!
            .topicResultToNamedResult("buildings")
        this += topicResults["highway"]!!
            .topicResultToNamedResult("roads")
        return this
    }

    private fun UserTopicResult.topicResultToNamedResult(name: String): Map<String, Any> {
        val renamed = mutableMapOf(
            name to this.value,
            "${name}_created" to this.added,
            "${name}_deleted" to this.deleted,
            "${name}_modified" to this.modified.count_modified
        )
        if (name == "roads") {
            renamed += mapOf(
                "${name}_modified_longer" to this.modified.unit_more!!,
                "${name}_modified_shorter" to this.modified.unit_less!!
            )
        }
        return renamed
    }

    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)


}
