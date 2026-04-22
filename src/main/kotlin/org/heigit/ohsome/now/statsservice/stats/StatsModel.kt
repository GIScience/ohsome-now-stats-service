package org.heigit.ohsome.now.statsservice.stats

import org.heigit.ohsome.now.statsservice.topic.TopicCountryResult
import org.heigit.ohsome.now.statsservice.topic.TopicIntervalResult
import org.heigit.ohsome.now.statsservice.topic.TopicResult
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneOffset


// used in hashtags endpoint, do not remove
fun Map<String, Any>.toStatsResult() = StatsResult(
    (this["changesets"] as Number).toLong(),
    (this["users"] as Number).toLong(),
    this["roads"] as Double,
    this["buildings"] as Long,
    (this["edits"] as Number).toLong(),
    (this["latest"] as Timestamp).toLocalDateTime().toInstant(ZoneOffset.UTC).toString()
)

@Suppress("LongParameterList")
data class StatsResultWithTopics(
    val topics: Map<String, TopicResult>
)

fun Map<String, TopicResult>.toStatsResult() = StatsResultWithTopics(this)

fun StatsResultWithTopics.toUserResult(userId: List<String>) = UserResult(
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
    data["number_of_users"].toString().toLong()
)


data class HashtagResult(
    val hashtag: String,
    val number_of_users: Long
)


fun Map<String, Any>.toMetadataResult(): MetadataResult =
    MetadataResult(
        (this["max_timestamp"] as Timestamp).toLocalDateTime().toInstant(ZoneOffset.UTC).toString(),
        (this["min_timestamp"] as Timestamp).toLocalDateTime().toInstant(ZoneOffset.UTC).toString()
    )


data class MetadataResult(
    val max_timestamp: String,
    val min_timestamp: String
)


data class UserResult(
    val topics: Map<String, TopicResult>,
    val userId: List<String>
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
