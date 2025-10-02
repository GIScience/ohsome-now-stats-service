package org.heigit.ohsome.now.statsservice.topic

import java.time.LocalDateTime


data class ModifiedSection(
    val count_modified: Long,
    val unit_more: Double?,
    val unit_less: Double?,
)

data class ModifiedArraySection(
    val count_modified: LongArray,
    val unit_more: DoubleArray?,
    val unit_less: DoubleArray?,
)

@Suppress("LongParameterList")
open class TopicResult(
    open val added: Double?,
    open val modified: ModifiedSection?,
    open val deleted: Double?,
    open val value: Double,
)


fun Map<String, Any>.toTopicResult(topic: String) = TopicResult(
    this["topic_result_created"]?.toString()?.toDouble(),
    if (topic in statsTopics) null else ModifiedSection(
        this["topic_result_modified"].toString().toDouble().toLong(),
        this["topic_result_modified_more"]?.toString()?.toDouble(),
        this["topic_result_modified_less"]?.toString()?.toDouble(),
    ),
    this["topic_result_deleted"]?.toString()?.toDouble(),
    this["topic_result"].toString().toDouble(),
)


@Suppress("LongParameterList")
open class TopicIntervalResult(
    val added: DoubleArray?,
    val modified: ModifiedArraySection?,
    val deleted: DoubleArray?,
    val value: DoubleArray,
    var startDate: Array<LocalDateTime>?,
    var endDate: Array<LocalDateTime>?
)

fun Map<String, Any>.toTopicIntervalResult(topic: String) = TopicIntervalResult(
    this["topic_result_created"] as? DoubleArray,
    if (topic in statsTopics) null else ModifiedArraySection(
        this["topic_result_modified"] as LongArray,
        this["topic_result_modified_more"] as? DoubleArray,
        this["topic_result_modified_less"] as? DoubleArray,
    ),
    this["topic_result_deleted"] as? DoubleArray,
    this["topic_result"] as DoubleArray,
    this["startdate"] as? Array<LocalDateTime>,
    this["enddate"] as? Array<LocalDateTime>,
)


@Suppress("LongParameterList")
open class TopicCountryResult(
    added: Double?,
    modified: ModifiedSection?,
    deleted: Double?,
    value: Double,
    open val country: String
) : TopicResult(added, modified, deleted, value)


fun List<Map<String, Any>>.toTopicCountryResult(topic: String) = this.map { topicCountryResult(it, topic) }


fun topicCountryResult(data: Map<String, Any>, topic: String) = TopicCountryResult(
    (data["topic_result_created"]?.toString())?.toDouble(),
    if (topic in statsTopics) null else ModifiedSection(
        (data["topic_result_modified"].toString()).toDouble().toLong(),
        (data["topic_result_modified_more"]?.toString())?.toDouble(),
        (data["topic_result_modified_less"]?.toString())?.toDouble(),
    ),
    (data["topic_result_deleted"]?.toString())?.toDouble(),
    (data["topic_result"].toString()).toDouble(),
    data["country"].toString()
)