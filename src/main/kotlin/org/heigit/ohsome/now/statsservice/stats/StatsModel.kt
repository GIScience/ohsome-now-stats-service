package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import java.time.LocalDateTime


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
    this["buildings"].toString().toDouble().toLong(),
    this["buildings_created"].toString().toDouble().toLong(),
    this["buildings_modified"].toString().toLong(),
    this["buildings_deleted"].toString().toDouble().toLong(),
    this["roads"].toString().toDouble(),
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
