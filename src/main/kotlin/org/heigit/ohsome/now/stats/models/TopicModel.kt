package org.heigit.ohsome.now.stats.models


fun Map<String, Any>.toTopicResult(topic: String) = TopicResult(
    this["hashtag"].toString(),
    topic,
    (this["topic_result"].toString()).toLong()
)

data class TopicResult(
    val hashtag: String,
    val topic: String,
    val value: Long,
)