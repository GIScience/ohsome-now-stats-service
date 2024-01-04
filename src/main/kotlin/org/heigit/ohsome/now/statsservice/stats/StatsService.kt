package org.heigit.ohsome.now.statsservice.stats

import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo


    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?, countries: List<String>) =
        this.repo
            .getStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries))
            .toStatsResult()


    fun getStatsForTimeSpanAggregate(hashtags: List<String>, startDate: Instant?, endDate: Instant?) = hashtags
        .map { getStatsForTimeSpanAggregate(it, startDate, endDate) }
        .reduce { m1, m2 -> m1 + m2 }


    private fun getStatsForTimeSpanAggregate(hashtag: String, startDate: Instant?, endDate: Instant?) = this.repo
        .getStatsForTimeSpanAggregate(handler(hashtag), startDate, endDate)
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
        .toIntervalStatsResult()


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
