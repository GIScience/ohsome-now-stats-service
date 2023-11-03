package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.CountryHandler
import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class StatsService {

    @Autowired
    lateinit var repo: StatsRepo


    fun getStatsForTimeSpan(hashtag: String, startDate: Instant?, endDate: Instant?, countries: List<String>) =
        this.repo.getStatsForTimeSpan(HashtagHandler(hashtag), startDate, endDate, CountryHandler(countries))


    fun getStatsForTimeSpanAggregate(hashtag: String, startDate: Instant?, endDate: Instant?) =
        this.repo.getStatsForTimeSpanAggregate(HashtagHandler(hashtag), startDate, endDate)


    fun getStatsForTimeSpanInterval(hashtag: String, startDate: Instant?, endDate: Instant?, interval: String, countries: List<String>) =
        this.repo.getStatsForTimeSpanInterval(HashtagHandler(hashtag), startDate, endDate, interval, CountryHandler(countries))


    fun getStatsForTimeSpanCountry(hashtag: String, startDate: Instant?, endDate: Instant?) =
        this.repo.getStatsForTimeSpanCountry(HashtagHandler(hashtag), startDate, endDate)


    fun getMostUsedHashtags(startDate: Instant?, endDate: Instant?, limit: Int?) =
        this.repo.getMostUsedHashtags(startDate, endDate, limit)


    fun getMetadata() = this.repo.getMetadata()


}
