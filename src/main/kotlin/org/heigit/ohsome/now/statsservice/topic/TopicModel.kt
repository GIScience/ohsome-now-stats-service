package org.heigit.ohsome.now.statsservice.topic


fun Map<String, Any>.toTopicResult(topic: String) = TopicResult(
    this["hashtag"].toString(),
    topic,
    (this["topic_result"].toString()).toDouble()
)

data class TopicResult(
    val hashtag: String,
    val topic: String,
    val value: Double,
)

//TODO: common superclass for all interval results?
open class TopicIntervalResult(
    open val value: Double,
    open val topic: String,
    open val startDate: String,
    open val endDate: String
)


fun List<Map<String, Any>>.toTopicIntervalResult(topic: String) = this.map { topicIntervalResult(it, topic) }


fun topicIntervalResult(data: Map<String, Any>, topic: String) = TopicIntervalResult(
    data["topic_result"].toString().toDouble(),
    topic,
    data["startdate"].toString(),
    data["enddate"].toString(),
)


//TODO: common superclass for all interval results?
open class TopicCountryResult(
    open val value: Double,
    open val topic: String,
    open val country: String
)


fun List<Map<String, Any>>.toTopicCountryResult(topic: String) = this.map { topicCountryResult(it, topic) }


fun topicCountryResult(data: Map<String, Any>, topic: String) = TopicCountryResult(
    data["topic_result"].toString().toDouble(),
    topic,
    data["country"].toString()
)

fun Map<String, Any>.toUserTopicResult(topic: String) = UserTopicResult(
    topic,
    this["topic_result"].toString().toDouble(),
    this["user_id"] as Int
)


data class UserTopicResult(
    val topic: String,
    val value: Double,
    val userId: Int
)
