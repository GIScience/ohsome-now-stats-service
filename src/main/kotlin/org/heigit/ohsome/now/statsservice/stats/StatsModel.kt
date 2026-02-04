package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.topic.TopicCountryResult
import org.heigit.ohsome.now.statsservice.topic.TopicIntervalResult
import org.heigit.ohsome.now.statsservice.topic.TopicResult
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_INSTANT


// used in hashtags endpoint, do not remove
fun Map<String, Any>.toStatsResult() = StatsResult(
    (this["changesets"] as UnsignedLong).toLong(),
    (this["users"] as UnsignedLong).toLong(),
    this["roads"] as Double,
    this["buildings"] as Long,
    (this["edits"] as UnsignedLong).toLong(),
    (this["latest"] as OffsetDateTime).format(ISO_INSTANT)
)

@Suppress("LongParameterList")
data class StatsResultWithTopics(
    val topics: Map<String, TopicResult>
)

fun Map<String, TopicResult>.toStatsResult() = StatsResultWithTopics(this)

fun StatsResultWithTopics.toUserResult(userId: String) = UserResult(
    this.topics,
    userId
)

data class StatsIntervalResultWithTopics(
    var startDate: Array<LocalDateTime>?,
    var endDate: Array<LocalDateTime>?,
    val topics: MutableMap<String, TopicIntervalResult>
)


fun List<Map<String, Any>>.toMultipleStatsResult() = this.associate {
    it["hashtag"].toString() to it.toStatsResult()
}

@Suppress("LongParameterList")
open class StatsResult(
    open val changesets: Long,
    open val users: Long,
    open val roads: Double,
    open val buildings: Long,
    open val edits: Long,
    open val latest: String
)

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
        (this["max_timestamp"] as OffsetDateTime).format(ISO_INSTANT),
        (this["min_timestamp"] as OffsetDateTime).format(ISO_INSTANT)
    )


data class MetadataResult(
    val max_timestamp: String,
    val min_timestamp: String
)


fun Map<String, TopicResult>.toUserResult(userId: String) = UserResult(
    this,
    userId
)


data class UserResult(
    val topics: Map<String, TopicResult>,
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
