package org.heigit.ohsome.now.statsservice.topic


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
                ")/ 1000, 0)"                               // m to km
    ),
    AREA(
        "ifNull(" +
                "sum(" +
                "multiIf(" +                                // this is a 'case'
                "edit = 1, area," +                         // case, then,
                "edit = 0, area_delta," +                   // case, then,
                "edit = -1, - area + area_delta," +         // case, then,
                "0)" +                                      // else
                ")/ 1000000, 0)"                            // square m to square km
    )

}


//TODO: consider ENUM for `beforeOrCurrent`
interface TopicDefinition {

    val topicName: String
    val aggregationStrategy: AggregationStrategy
    fun keys(): List<String>

    fun buildValueLists(): String
    fun beforeCurrentCondition(beforeOrCurrent: String): String
    fun defineTopicResult(): String

}


class KeyValueTopicDefinition(
    override val topicName: String,
    private val matchers: List<TagMatcher>,
    override val aggregationStrategy: AggregationStrategy = AggregationStrategy.COUNT
) : TopicDefinition {


    override fun keys() = matchers.map { it.key }

    override fun defineTopicResult() = aggregationStrategy.sql


    override fun buildValueLists() = matchers
        .map(TagMatcher::getSingleAllowedValuesList)
        .filter(String::isNotEmpty)
        .map { "$it,\n" }
        .joinToString("")


    override fun beforeCurrentCondition(beforeOrCurrent: String) = this.matchers
        .map { it.getSingleBeforeOrCurrentCondition(beforeOrCurrent) }
        .joinToString("OR ")
        .plus("as ${beforeOrCurrent},\n")


}


interface TagMatcher {

    val key: String
    fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: String): String
    fun getSingleAllowedValuesList(): String

}


class KeyValueMatcher(override val key: String, private val allowedValues: List<String>): TagMatcher {

    override fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: String) =
        "${this.key}_${beforeOrCurrent} in ${this.key}_tags\n"


    override fun getSingleAllowedValuesList() = this.allowedValues
        .filter(String::isNotBlank)
        .map { "'$it'" }
        .let { "${it} as ${this.key}_tags" }

}


class KeyOnlyMatcher(override val key: String): TagMatcher {

    override fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: String) = " ${key}_${beforeOrCurrent} <> '' "

    override fun getSingleAllowedValuesList() = ""

}

