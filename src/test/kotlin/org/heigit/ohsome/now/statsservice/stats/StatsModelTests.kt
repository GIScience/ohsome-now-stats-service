package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.topic.TopicResult
import org.heigit.ohsome.now.statsservice.topic.toTopicCountryResult
import org.heigit.ohsome.now.statsservice.topic.toTopicResult
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime


class StatsModelTests {

    private val hashtag1 = "hotosm-123"
    private val hashtag2 = "missing_maps"
    private val map1: Map<String, Any> = createMap(hashtag1)
    private val map2: Map<String, Any> = createMap(hashtag2)
    private val topicMap1: Map<String, TopicResult> = createTopicMap();

    private fun createMap(hashtag: String) = mapOf(
        "hashtag" to hashtag,
        "changesets" to UnsignedLong.valueOf(2),
        "users" to UnsignedLong.valueOf(1001L),
        "roads" to 43534.5,
        "buildings" to 123L,
        "edits" to UnsignedLong.valueOf(213124L),
        "latest" to OffsetDateTime.parse("2021-12-09T13:01:28Z"),
    )

    private fun createTopicMap() = mapOf(
        "changeset" to mapOf("topic_result" to 2.0).toTopicResult("changeset"),
        "contributor" to mapOf("topic_result" to 1001.0).toTopicResult("contributor"),
        "road" to mapOf("topic_result" to 43534.5, "topic_result_modified" to 10).toTopicResult("road"),
        "building" to mapOf("topic_result" to 123.0, "topic_result_modified" to 10).toTopicResult("building"),
        "edit" to mapOf("topic_result" to 213124.0).toTopicResult("edit"),
    )


    @Test
    fun toStatsResult() {
        val result = topicMap1.toStatsResult()

        assertThat(result.topics["changeset"]?.value).isEqualTo(2.0)
        assertThat(result.topics["contributor"]?.value).isEqualTo(1001.0)
        assertThat(result.topics["road"]?.value).isEqualTo(43534.5)
        assertThat(result.topics["building"]?.value).isEqualTo(123.0)
        assertThat(result.topics["edit"]?.value).isEqualTo(213124.0)
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
    fun toCountryStatsResult() {

        val maps = listOf(mapOf("topic_result" to 2.0)).toTopicCountryResult("edit")
        val result = mapOf("edit" to maps).toStatsTopicCountryResult()

        assertThat(result.topics["edit"]!![0].value)
            .isEqualTo(
                2.0
            )
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