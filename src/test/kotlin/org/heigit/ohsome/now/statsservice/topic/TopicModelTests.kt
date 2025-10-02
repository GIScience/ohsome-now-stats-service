package org.heigit.ohsome.now.statsservice.topic

import com.clickhouse.data.value.UnsignedLong
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class TopicModelTests {

    private val hashtag = "hotosm-123"


    @Test
    fun toTopicResultForClickhouseLong() {
        val map5 = mapOf(
            "topic_result" to UnsignedLong.valueOf(20),
            "topic_result_modified" to UnsignedLong.valueOf(0L),
            "topic_result_created" to UnsignedLong.valueOf(20L),
            "topic_result_deleted" to UnsignedLong.valueOf(0L)
        )
        val topic = "place"
        val result = map5.toTopicResult(topic)

        assertThat(result.value).isEqualTo(20.0)
        assertThat(result.added).isEqualTo(20.0)
        assertThat(result.modified?.count_modified).isEqualTo(0L)
    }


    @Test
    fun toTopicResultForJavaLong() {
        val map5 = mapOf(
            "topic_result" to 20L,
            "topic_result_modified" to 0L,
            "topic_result_created" to 20L,
            "topic_result_deleted" to 0L
        )
        val topic = "place"
        val result = map5.toTopicResult(topic)

        assertThat(result.value).isEqualTo(20.0)
        assertThat(result.added).isEqualTo(20.0)
        assertThat(result.modified?.count_modified).isEqualTo(0L)
    }

}