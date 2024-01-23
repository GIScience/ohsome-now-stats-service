package org.heigit.ohsome.now.statsservice.topic


fun Map<String, Any>.toTopicResult(topic: String) = TopicResult(
    topic,
    (this["topic_result_created"].toString()).toDouble(),
    ModifiedSection(
        (this["topic_result_modified"].toString()).toDouble().toLong(),
        (this["topic_result_modified_more"]?.toString())?.toDouble(),
        (this["topic_result_modified_less"]?.toString())?.toDouble(),
    ),
    (this["topic_result_deleted"].toString()).toDouble(),
    (this["topic_result"].toString()).toDouble()
)

@Suppress("LongParameterList")
open class TopicResult(
    open val topic: String,
    open val added: Double,
    open val modified: ModifiedSection,
    open val deleted: Double,
    open val value: Double
)


data class ModifiedSection(
    val count_modified: Long,
    val unit_more: Double?,
    val unit_less: Double?,
)

@Suppress("LongParameterList")
open class TopicIntervalResult(
    topic: String,
    added: Double,
    modified: ModifiedSection,
    deleted: Double,
    value: Double,
    open val startDate: String,
    open val endDate: String
) : TopicResult(topic, added, modified, deleted, value)


fun List<Map<String, Any>>.toTopicIntervalResult(topic: String) = this.map { topicIntervalResult(it, topic) }


fun topicIntervalResult(data: Map<String, Any>, topic: String) = TopicIntervalResult(
    topic,
    (data["topic_result_created"].toString()).toDouble(),
    ModifiedSection(
        (data["topic_result_modified"].toString()).toDouble().toLong(),
        (data["topic_result_modified_more"]?.toString())?.toDouble(),
        (data["topic_result_modified_less"]?.toString())?.toDouble(),
    ),
    (data["topic_result_deleted"].toString()).toDouble(),
    (data["topic_result"].toString()).toDouble(),
    data["startdate"].toString(),
    data["enddate"].toString(),
)

@Suppress("LongParameterList")
open class TopicCountryResult(
    topic: String,
    added: Double,
    modified: ModifiedSection,
    deleted: Double,
    value: Double,
    open val country: String
) : TopicResult(topic, added, modified, deleted, value)


fun List<Map<String, Any>>.toTopicCountryResult(topic: String) = this.map { topicCountryResult(it, topic) }


fun topicCountryResult(data: Map<String, Any>, topic: String) = TopicCountryResult(
    topic,
    (data["topic_result_created"].toString()).toDouble(),
    ModifiedSection(
        (data["topic_result_modified"].toString()).toDouble().toLong(),
        (data["topic_result_modified_more"]?.toString())?.toDouble(),
        (data["topic_result_modified_less"]?.toString())?.toDouble(),
    ),
    (data["topic_result_deleted"].toString()).toDouble(),
    (data["topic_result"].toString()).toDouble(),
    data["country"].toString()
)

@Suppress("LongParameterList")
open class UserTopicResult(
    topic: String,
    added: Double,
    modified: ModifiedSection,
    deleted: Double,
    value: Double,
    open val userId: Int
) : TopicResult(topic, added, modified, deleted, value)


fun Map<String, Any>.toUserTopicResult(topic: String) = UserTopicResult(
    topic,
    this["topic_result_created"].toString().toDouble(),
    ModifiedSection(
        this["topic_result_modified"].toString().toDouble().toLong(),
        this["topic_result_modified_more"]?.toString()?.toDouble(),
        this["topic_result_modified_less"]?.toString()?.toDouble(),
    ),
    this["topic_result_deleted"].toString().toDouble(),
    this["topic_result"].toString().toDouble(),
    this["user_id"] as Int
)
