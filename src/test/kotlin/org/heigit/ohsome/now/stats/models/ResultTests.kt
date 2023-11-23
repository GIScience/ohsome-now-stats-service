package org.heigit.ohsome.now.stats.models

import com.clickhouse.data.value.UnsignedLong
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class ResultTests {

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
        "latest" to "20.05.2053",
    )


    @Test
    fun toStatsResult() {

        val result = map1.toStatsResult()

        assertThat(result.changesets).isEqualTo(2L)
        assertThat(result.users).isEqualTo(1001L)
        assertThat(result.roads).isEqualTo(43534.5)
        assertThat(result.buildings).isEqualTo(123L)
        assertThat(result.edits).isEqualTo(213124L)
        assertThat(result.latest).isEqualTo("20.05.2053")

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

        val maps = listOf(this.map1, this.map2)
        val result = maps.toIntervalStatsResult()

        assertThat(result[0])
            .usingRecursiveComparison()
            .isEqualTo(statsIntervalResult(map1))

        assertThat(result[1])
            .usingRecursiveComparison()
            .isEqualTo(statsIntervalResult(map2))

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

    @Test
    fun toTopicResultForClickhouseLong() {
        val map5 = mapOf(
            "hashtag" to hashtag1,
            "topic_result" to UnsignedLong.valueOf(20),
        )
        val topic = "place"
        val result = map5.toTopicResult(topic)

        assertThat(result.hashtag).isEqualTo(hashtag1)
        assertThat(result.topic).isEqualTo(topic)
        assertThat(result.value).isEqualTo(20L)
    }


    @Test
    fun toTopicResultForJavaLong() {
        val map5 = mapOf(
            "hashtag" to hashtag1,
            "topic_result" to 20L,
        )
        val topic = "place"
        val result = map5.toTopicResult(topic)

        assertThat(result.hashtag).isEqualTo(hashtag1)
        assertThat(result.topic).isEqualTo(topic)
        assertThat(result.value).isEqualTo(20L)
    }

}