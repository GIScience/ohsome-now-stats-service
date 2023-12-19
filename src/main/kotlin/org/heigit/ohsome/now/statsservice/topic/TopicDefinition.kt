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


//TODO: delete deprecated class
@Deprecated("goal: 1 topic definition, 2 matchers")
class KeyOnlyTopicDefinition(
    override val topicName: String,
    key: String,
    override val aggregationStrategy: AggregationStrategy = AggregationStrategy.COUNT
) : TopicDefinition {

    private val matcher = KeyOnlyMatcher(key)

    override fun keys() = listOf(matcher.key)

    override fun defineTopicResult() = aggregationStrategy.sql

    override fun buildValueLists() = matcher.getSingleAllowedValuesList()

    override fun beforeCurrentCondition(beforeOrCurrent: String) = matcher.getSingleBeforeOrCurrentCondition(beforeOrCurrent)

}


class KeyValueTopicDefinition(
    override val topicName: String, val matchers: List<TagMatcher>,
    override val aggregationStrategy: AggregationStrategy = AggregationStrategy.COUNT
) : TopicDefinition {


    override fun keys() = matchers.map { it.key }

    override fun defineTopicResult() = aggregationStrategy.sql


    override fun buildValueLists(): String {
        val matcherList = this.matchers

        val valueLists = matcherList
            .map(TagMatcher::getSingleAllowedValuesList)

        val filtered = valueLists
            .filter { it.length > 0 }


        val result = filtered.map { "$it,\n" }

        return result.joinToString("")
    }


    override fun beforeCurrentCondition(beforeOrCurrent: String): String {
        var temp = ""
        for (matcher in this.matchers) {
            // add "OR " between conditions
            if (temp != "") temp += "OR "

            temp += matcher.getSingleBeforeOrCurrentCondition(beforeOrCurrent)
        }
        temp += "as ${beforeOrCurrent},\n"
        return temp
    }

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

