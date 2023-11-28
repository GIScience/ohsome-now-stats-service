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

//TODO: common superclass for all interval results?
open class TopicIntervalResult(
    open val value: Long,
    open val topic: String,
    open val startDate: String,
    open val endDate: String
)


fun List<Map<String, Any>>.toTopicIntervalResult(topic: String) = this.map { topicIntervalResult(it, topic) }


fun topicIntervalResult(data: Map<String, Any>, topic: String) = TopicIntervalResult(
    data["topic_result"].toString().toLong(),
    topic,
    data["startdate"].toString(),
    data["enddate"].toString(),
)




//TODO: common superclass for all interval results?
open class TopicCountryResult(
    open val value: Long,
    open val topic: String,
    open val Country: String
)


fun List<Map<String, Any>>.toTopicCountryResult(topic: String) = this.map { topicCountryResult(it, topic) }


fun topicCountryResult(data: Map<String, Any>, topic: String) = TopicCountryResult(
    data["topic_result"].toString().toLong(),
    topic,
    data["country"].toString()
)



