package org.heigit.ohsome.now.stats.models

import com.clickhouse.data.value.UnsignedLong


fun Map<String, Any>.toStatsResult() = StatsResult(
    (this["changesets"] as UnsignedLong).toLong(),
    (this["users"] as UnsignedLong).toLong(),
    this["roads"] as Double,
    this["buildings"] as Long,
    (this["edits"] as UnsignedLong).toLong(),
    this["latest"].toString(),
)


//TODO: methode drüber nutzen?
fun List<Map<String, Any>>.toMultipleStatsResult(): Map<String, StatsResult> {
    val output = mutableMapOf<String, StatsResult>()
    this.forEach {
        output[it["hashtag"].toString()] = StatsResult(
            (it["changesets"] as UnsignedLong).toLong(),
            (it["users"] as UnsignedLong).toLong(),
            it["roads"] as Double,
            it["buildings"] as Long,
            (it["edits"] as UnsignedLong).toLong(),
            it["latest"].toString(),
        )
    }
    return output
}


//TODO: cleanup
fun List<Map<String, Any>>.buildIntervalStatsResult(): List<StatsIntervalResult> {
    val output = mutableListOf<StatsIntervalResult>()
    this.forEach {
        output.add(statsIntervalResult(it))
    }

    return output
}


fun statsIntervalResult(data: Map<String, Any>) = StatsIntervalResult(
    (data["changesets"] as UnsignedLong).toLong(),
    (data["users"] as UnsignedLong).toLong(),
    data["roads"] as Double,
    data["buildings"] as Long,
    (data["edits"] as UnsignedLong).toLong(),
    data["startdate"].toString(),
    data["enddate"].toString(),
)

@Suppress("LongMethod")
fun buildCountryStatsResult(result: List<Map<String, Any>>): List<CountryStatsResult> {
    val output = mutableListOf<CountryStatsResult>()
    result.forEach {
        output.add(
            CountryStatsResult(
                (it["changesets"] as UnsignedLong).toLong(),
                (it["users"] as UnsignedLong).toLong(),
                it["roads"] as Double,
                it["buildings"] as Long,
                (it["edits"] as UnsignedLong).toLong(),
                it["latest"].toString(),
                it["country"].toString(),
            )
        )
    }
    return output
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


fun buildHashtagResult(result: List<Map<String, Any>>): List<HashtagResult> {
    val output = mutableListOf<HashtagResult>()
    result.forEach {
        output.add(
            HashtagResult(
                it["hashtag"].toString(),
                (it["number_of_users"] as UnsignedLong).toLong()
            )
        )
    }
    return output
}

data class HashtagResult(
    val hashtag: String,
    val number_of_users: Long
)


fun buildMetadataResult(result: Map<String, Any>): MetadataResult =
    MetadataResult(
        result["max_timestamp"].toString(),
        result["min_timestamp"].toString()
    )

data class MetadataResult(
    val max_timestamp: String,
    val min_timestamp: String
)


fun buildUserResult(result: Map<String, Any>) =
    UserResult(
        result["buildings"] as Long,
        result["roads"] as Double,
        (result["edits"] as UnsignedLong).toLong(),
        (result["changesets"] as UnsignedLong).toLong(),
        result["user_id"] as Int
    )

data class UserResult(
    val buildings: Long,
    val roads: Double,
    val edits: Long,
    val changesets: Long,
    val userId: Int
)
