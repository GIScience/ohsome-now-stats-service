package org.heigit.ohsome.now.statsservice.models

import com.clickhouse.data.value.UnsignedLong
import org.assertj.core.api.Assertions.assertThat
import org.heigit.ohsome.now.statsservice.topic.toTopicResult
import org.junit.jupiter.api.Test


class TopicModelTests {

    private val hashtag = "hotosm-123"


    @Test
    fun toTopicResultForClickhouseLong() {
        val map5 = mapOf(
            "hashtag" to hashtag,
            "topic_result" to UnsignedLong.valueOf(20),
        )
        val topic = "place"
        val result = map5.toTopicResult(topic)

        assertThat(result.hashtag).isEqualTo(hashtag)
        assertThat(result.topic).isEqualTo(topic)
        assertThat(result.value).isEqualTo(20L)
    }


    @Test
    fun toTopicResultForJavaLong() {
        val map5 = mapOf(
            "hashtag" to hashtag,
            "topic_result" to 20L,
        )
        val topic = "place"
        val result = map5.toTopicResult(topic)

        assertThat(result.hashtag).isEqualTo(hashtag)
        assertThat(result.topic).isEqualTo(topic)
        assertThat(result.value).isEqualTo(20L)
    }

}