package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.AggregationStrategy.COUNT


enum class AggregationStrategy(val sql: String) {
    COUNT("ifNull(sum(edit), 0)"),
    LENGTH(
        "ifNull(" +
                "sum(" +
                "multiIf(" +                                // this is a 'case'
                "edit = 1, length," +                       // case, then,
                "edit = 0, length_delta," +                 // case, then,
                "edit = -1, - length + length_delta," +     // case, then,
                "0)" +                                      // else
                ")/ 1000, 0)"
    )
}


interface TopicDefinition {

    val topicName: String
    fun keys(): List<String>

    fun buildValueLists(): String
    fun beforeCurrentCondition(beforeOrCurrent: String): String
    fun defineTopicResult(): String

}


class KeyOnlyTopicDefinition(
    override val topicName: String,
    val key: String,
    val aggregationStrategy: AggregationStrategy = COUNT
) : TopicDefinition {

    override fun keys() = listOf(key)

    override fun defineTopicResult() = aggregationStrategy.sql

    override fun buildValueLists() = ""

    override fun beforeCurrentCondition(beforeOrCurrent: String) =
        " ${key}_${beforeOrCurrent} <> '' as ${beforeOrCurrent}, "

}


class KeyValueTopicDefinition(
    override val topicName: String, val matchers: List<KeyValueMatcher>,
    val aggregationStrategy: AggregationStrategy = COUNT
) : TopicDefinition {


    override fun keys() = matchers.map { it.key }

    override fun defineTopicResult() = aggregationStrategy.sql


    override fun buildValueLists(): String {
        var valueLists = ""

        for (matcher in this.matchers) {
            val allowedValuesList = matcher.allowedValues
                .filter(String::isNotBlank)
                .map { "'$it'" }

            valueLists += "${allowedValuesList} as ${matcher.key}_tags\n,"
        }

        return valueLists
    }


    override fun beforeCurrentCondition(beforeOrCurrent: String): String {
        var temp = ""
        for (matcher in this.matchers) {
            // add "OR " between conditions
            if (temp != "") temp += "OR "

            temp += "${matcher.key}_${beforeOrCurrent} in ${matcher.key}_tags\n"
        }
        temp += "as ${beforeOrCurrent},\n"
        return temp
    }

}


class KeyValueMatcher(
    val key: String,
    val allowedValues: List<String>
)

