package org.heigit.ohsome.now.statsservice.stats

import org.heigit.ohsome.now.statsservice.topic.TopicHandler
import org.heigit.ohsome.now.statsservice.topic.TopicRepo
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo

    @Autowired
    lateinit var topicRepo: TopicRepo


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
        .toCountryStatsResult()


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
        .toUserResult()


    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)


}
