package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.topic.TopicCountryResult
import org.heigit.ohsome.now.statsservice.topic.TopicIntervalResultMinusTopic
import org.heigit.ohsome.now.statsservice.topic.TopicResult
import org.heigit.ohsome.now.statsservice.topic.TopicResultMinusTopic
import java.time.LocalDateTime


// used in hashtags endpoint, do not remove
fun Map<String, Any>.toStatsResult() = StatsResult(
    (this["changesets"] as UnsignedLong).toLong(),
    (this["users"] as UnsignedLong).toLong(),
    this["roads"] as Double,
    this["buildings"] as Long,
    (this["edits"] as UnsignedLong).toLong(),
    this["latest"].toString(),
)

@Suppress("LongParameterList")
data class StatsResultWithTopics(
    val topics: Map<String, TopicResult>
)

fun Map<String, TopicResult>.toStatsResult() = StatsResultWithTopics(this)

data class StatsIntervalResultWithTopics(
    var startDate: Array<LocalDateTime>?,
    var endDate: Array<LocalDateTime>?,
    val topics: MutableMap<String, TopicIntervalResultMinusTopic>
)


fun List<Map<String, Any>>.toMultipleStatsResult() = this.associate {
    it["hashtag"].toString() to it.toStatsResult()
}


fun Map<String, Any>.toIntervalStatsResult() =
    StatsIntervalResult(
        this["changesets"] as LongArray,
        this["users"] as LongArray,
        this["roads"] as DoubleArray,
        this["buildings"] as DoubleArray,
        this["edits"] as LongArray,
        this["startdate"] as Array<LocalDateTime>,
        this["enddate"] as Array<LocalDateTime>,
    )


fun List<Map<String, Any>>.toCountryStatsResult() = this.map(::countryStatsResult)


fun countryStatsResult(data: Map<String, Any>) = CountryStatsResult(
    (data["changesets"] as UnsignedLong).toLong(),
    (data["users"] as UnsignedLong).toLong(),
    data["roads"] as Double,
    data["buildings"] as Long,
    (data["edits"] as UnsignedLong).toLong(),
    data["latest"].toString(),
    data["country"].toString(),
)


@Suppress("LongParameterList")
open class StatsResult(
    open val changesets: Long,
    open val users: Long,
    open val roads: Double,
    open val buildings: Long,
    open val edits: Long,
    open val latest: String
)


@Suppress("LongParameterList")
data class StatsIntervalResult(
    val changesets: LongArray,
    val users: LongArray,
    val roads: DoubleArray,
    val buildings: DoubleArray,
    val edits: LongArray,
    val startDate: Array<LocalDateTime>,
    val endDate: Array<LocalDateTime>
)


@Suppress("LongParameterList")
class CountryStatsResult(
    changesets: Long,
    users: Long,
    roads: Double,
    buildings: Long,
    edits: Long,
    latest: String,
    val country: String
) : StatsResult(changesets, users, roads, buildings, edits, latest)


fun Map<String, List<TopicCountryResult>>.toStatsTopicCountryResult() = StatsTopicCountryResult(this)


data class StatsTopicCountryResult(
    val topics: Map<String, List<TopicCountryResult>>,
)


fun List<Map<String, Any>>.toHashtagResult() = this.map(::hashtagResult)


fun hashtagResult(data: Map<String, Any>) = HashtagResult(
    data["hashtag"].toString(),
    (data["number_of_users"] as UnsignedLong).toLong()
)


data class HashtagResult(
    val hashtag: String,
    val number_of_users: Long
)


fun Map<String, Any>.toMetadataResult(): MetadataResult =
    MetadataResult(
        this["max_timestamp"].toString(),
        this["min_timestamp"].toString()
    )


data class MetadataResult(
    val max_timestamp: String,
    val min_timestamp: String
)


fun Map<String, TopicResultMinusTopic>.toUserResult(userId: String) = UserResult(
    this,
    userId
)


data class UserResult(
    val topics: Map<String, TopicResultMinusTopic>,
    val userId: String
)


fun List<Map<String, Any>>.toUniqueHashtagsResult(): List<UniqueHashtagsResult> = this.map {
    UniqueHashtagsResult(
        it["hashtag"].toString(),
        it["count"].toString().toLong()
    )
}


data class UniqueHashtagsResult(
    val hashtag: String,
    val count: Long
)
