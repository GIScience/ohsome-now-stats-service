package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime


class StatsModelTests {

    private val hashtag1 = "hotosm-123"
    private val hashtag2 = "missing_maps"
    private val map1: Map<String, Any> = createMap(hashtag1)
    private val map2: Map<String, Any> = createMap(hashtag2)


    private fun createMap(hashtag: String) = mapOf(
        "hashtag" to hashtag,

        "changesets" to UnsignedLong.valueOf(2),
        "users" to UnsignedLong.valueOf(1001L),
        "roads" to 43534.5,
        "buildings" to 123L,
        "edits" to UnsignedLong.valueOf(213124L),
        "latest" to OffsetDateTime.parse("2021-12-09T13:01:28Z"),
    )

    private val intervalMap = mapOf(
        "changesets" to longArrayOf(2L),
        "users" to longArrayOf(1001L),
        "roads" to doubleArrayOf(43534.5),
        "buildings" to doubleArrayOf(123.0),
        "edits" to longArrayOf(213124L),
        "startdate" to arrayOf(LocalDateTime.parse("2020-05-20T00:00:00")),
        "enddate" to arrayOf(LocalDateTime.parse("2023-05-20T00:00:00")),
    )

    private var exampleIntervalStatsData = StatsIntervalResult(
        intervalMap["changesets"] as LongArray,
        intervalMap["users"] as LongArray,
        intervalMap["roads"] as DoubleArray,
        intervalMap["buildings"] as DoubleArray,
        intervalMap["edits"] as LongArray,
        intervalMap["startdate"] as Array<LocalDateTime>,
        intervalMap["enddate"] as Array<LocalDateTime>
    )


    @Test
    fun toStatsResult() {

        val result = map1.toStatsResult()

        assertThat(result.changesets).isEqualTo(2L)
        assertThat(result.users).isEqualTo(1001L)
        assertThat(result.roads).isEqualTo(43534.5)
        assertThat(result.buildings).isEqualTo(123L)
        assertThat(result.edits).isEqualTo(213124L)
        assertThat(result.latest).isEqualTo("2021-12-09T13:01:28Z")
    }


    @Test
    fun toMultipleStatsResult() {

        val maps = listOf(this.map1, this.map2)
        val result = maps.toMultipleStatsResult()

        assertThat(result[hashtag1])
            .usingRecursiveComparison()
            .isEqualTo(map1.toStatsResult())

        assertThat(result[hashtag2])
            .usingRecursiveComparison()
            .isEqualTo(map2.toStatsResult())
    }


    @Test
    fun toIntervalStatsResult() {
        val result = intervalMap.toIntervalStatsResult()

        assertThat(result)
            .usingRecursiveComparison()
            .isEqualTo(exampleIntervalStatsData)
    }


    @Test
    fun toCountryStatsResult() {

        val maps = listOf(this.map1, this.map2)
        val result = maps.toCountryStatsResult()

        assertThat(result[0])
            .usingRecursiveComparison()
            .isEqualTo(countryStatsResult(map1))

        assertThat(result[1])
            .usingRecursiveComparison()
            .isEqualTo(countryStatsResult(map2))
    }


    @Test
    fun toHashtagResult() {

        val map3 = mapOf(
            "hashtag" to hashtag1,
            "number_of_users" to UnsignedLong.valueOf(2),
        )

        val map4 = mapOf(
            "hashtag" to hashtag2,
            "number_of_users" to UnsignedLong.valueOf(3),
        )

        val maps = listOf(map3, map4)
        val result = maps.toHashtagResult()

        assertThat(result[0])
            .usingRecursiveComparison()
            .isEqualTo(hashtagResult(map3))

        assertThat(result[1])
            .usingRecursiveComparison()
            .isEqualTo(hashtagResult(map4))
    }
}