package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import java.util.*


fun Map<String, Any>.toStatsResult() = StatsResult(
    (this["changesets"] as UnsignedLong).toLong(),
    (this["users"] as UnsignedLong).toLong(),
    this["roads"] as Double,
    this["buildings"] as Long,
    (this["edits"] as UnsignedLong).toLong(),
    this["latest"].toString(),
)


fun List<Map<String, Any>>.toMultipleStatsResult() = this.associate {
    it["hashtag"].toString() to it.toStatsResult()
}


fun List<Map<String, Any>>.toIntervalStatsResult() = this.map(::statsIntervalResult)


fun statsIntervalResult(data: Map<String, Any>) = StatsIntervalResult(
    (data["changesets"] as UnsignedLong).toLong(),
    (data["users"] as UnsignedLong).toLong(),
    data["roads"] as Double,
    data["buildings"] as Long,
    (data["edits"] as UnsignedLong).toLong(),
    data["startdate"].toString(),
    data["enddate"].toString(),
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
open class StatsIntervalResult(
    open val changesets: Long,
    open val users: Long,
    open val roads: Double,
    open val buildings: Long,
    open val edits: Long,
    open val startDate: String,
    open val endDate: String
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


fun Map<String, Any>.toUserResult() = UserResult(
    this["buildings"] as Long,
    this["buildings_created"].toString().toLong(),
    this["buildings_modified"].toString().toLong(),
    this["buildings_deleted"].toString().toLong(),
    this["roads"] as Double,
    this["roads_created"].toString().toDouble(),
    this["roads_modified_longer"].toString().toDouble(),
    this["roads_modified_shorter"].toString().toDouble(),
    this["roads_deleted"].toString().toDouble(),
    (this["edits"] as UnsignedLong).toLong(),
    (this["changesets"] as UnsignedLong).toLong(),
    this["user_id"] as Int
)


data class UserResult(
    val buildings: Long,
    val buildings_added: Long,
    val buildings_modified: Long,
    val buidlings_deleted: Long,
    val roads: Double,
    val roads_created_km: Double,
    val roads_modified_longer_km: Double,
    val roads_modified_shorter_km: Double,
    val roads_deleted_km: Double,
    val edits: Long,
    val changesets: Long,
    val userId: Int
)


fun List<String>.toUniqueHashtagsResult() = UniqueHashtagsResult(
    this
)

data class UniqueHashtagsResult(
    val hashtags: List<String>
)