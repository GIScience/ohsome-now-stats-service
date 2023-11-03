package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.models.toStatsResult
import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo


    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?, countries: List<String>) = this.repo
        .getStatsForTimeSpan(handler(hashtag), startDate, endDate, handler(countries))
        .toStatsResult()


    fun getStatsForTimeSpanAggregate(hashtag: String, startDate: Instant?, endDate: Instant?) =
        this.repo.getStatsForTimeSpanAggregate(handler(hashtag), startDate, endDate)


    @Suppress("LongParameterList")
    fun getStatsForTimeSpanInterval(hashtag: String, startDate: Instant?, endDate: Instant?, interval: String, countries: List<String>) =
        this.repo.getStatsForTimeSpanInterval(handler(hashtag), startDate, endDate, interval, handler(countries))


    fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?) =
        this.repo.getStatsForTimeSpanCountry(handler(hashtag), startDate, endDate)


    fun getMostUsedHashtags(startDate: Instant?, endDate: Instant?, limit: Int?) =
        this.repo.getMostUsedHashtags(startDate, endDate, limit)


    fun getMetadata() = this.repo.getMetadata()


    private fun handler(hashtag: String) = HashtagHandler(hashtag)
    private fun handler(countries: List<String>) = CountryHandler(countries)

}
