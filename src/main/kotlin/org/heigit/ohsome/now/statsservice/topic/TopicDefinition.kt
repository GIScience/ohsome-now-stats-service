package org.heigit.ohsome.now.statsservice.topic


enum class AggregationStrategy(val sql: String) {
    COUNT(
        """
            ifNull(sum(edit), 0) as topic_result,
            ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
            ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
            ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted
        """

    ),
    LENGTH(
        "ifNull(" +
                "sum(" +
                "multiIf(" +                                // this is a 'case'
                "edit = 1, length," +                       // case, then,
                "edit = 0, length_delta," +                 // case, then,
                "edit = -1, - length + length_delta," +     // case, then,
                "0)" +                                      // else
                ")/ 1000, 0) as topic_result"               // m to km
    ),
    AREA(
        "ifNull(" +
                "sum(" +
                "multiIf(" +                                // this is a 'case'
                "edit = 1, area," +                         // case, then,
                "edit = 0, area_delta," +                   // case, then,
                "edit = -1, - area + area_delta," +         // case, then,
                "0)" +                                      // else
                ")/ 1000000, 0) as topic_result"            // square m to square km
    )

}


enum class BeforeOrCurrent(val value: String) {
    BEFORE("before"),
    CURRENT("current")
}


class TopicDefinition(
    val topicName: String,
    private val matchers: List<TagMatcher>,
    val aggregationStrategy: AggregationStrategy = AggregationStrategy.COUNT
) {


    fun keys() = matchers.map { it.key }

    fun defineTopicResult() = aggregationStrategy.sql


    fun buildValueLists() = matchers
        .map(TagMatcher::getSingleAllowedValuesList)
        .filter(String::isNotEmpty)
        .map { "$it,\n" }
        .joinToString("")


    fun beforeCurrentCondition(beforeOrCurrent: BeforeOrCurrent) = this.matchers
        .map { it.getSingleBeforeOrCurrentCondition(beforeOrCurrent) }
        .joinToString("OR ")
        .plus("as ${beforeOrCurrent.value},\n")


}


interface TagMatcher {

    val key: String
    fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: BeforeOrCurrent): String
    fun getSingleAllowedValuesList(): String

}


class KeyValueMatcher(override val key: String, private val allowedValues: List<String>) : TagMatcher {

    override fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: BeforeOrCurrent) =
        "${this.key}_${beforeOrCurrent.value} in ${this.key}_tags\n"


    override fun getSingleAllowedValuesList() = this.allowedValues
        .filter(String::isNotBlank)
        .map { "'$it'" }
        .let { "${it} as ${this.key}_tags" }

}

class KeyNotValueMatcher(override val key: String, private val allowedValues: List<String>) : TagMatcher {

    override fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: BeforeOrCurrent) =
        "${this.key}_${beforeOrCurrent.value} not in ${this.key}_tags\n"


    override fun getSingleAllowedValuesList() = this.allowedValues
        .filter(String::isNotBlank)
        .map { "'$it'" }
        .plus("''")
        .let { "${it} as ${this.key}_tags" }

}

class KeyOnlyMatcher(override val key: String) : TagMatcher {

    override fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: BeforeOrCurrent) =
        " ${key}_${beforeOrCurrent.value} <> '' "

    override fun getSingleAllowedValuesList() = ""

}

