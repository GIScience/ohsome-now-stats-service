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


class KeyOnlyTopicDefinition(
    override val topicName: String,
    val key: String,
    override val aggregationStrategy: AggregationStrategy = AggregationStrategy.COUNT
) : TopicDefinition {

    override fun keys() = listOf(key)

    override fun defineTopicResult() = aggregationStrategy.sql

    override fun buildValueLists() = ""

    override fun beforeCurrentCondition(beforeOrCurrent: String) =
        " ${key}_${beforeOrCurrent} <> '' as ${beforeOrCurrent}, "

}


class KeyValueTopicDefinition(
    override val topicName: String, val matchers: List<KeyValueMatcher>,
    override val aggregationStrategy: AggregationStrategy = AggregationStrategy.COUNT
) : TopicDefinition {


    override fun keys() = matchers.map { it.key }

    override fun defineTopicResult() = aggregationStrategy.sql


    override fun buildValueLists(): String {
        var valueLists = ""

        for (matcher in this.matchers) {
            valueLists += matcher.getSingleAllowedValuesList()
        }

        return valueLists
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

    fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: String): String
    fun getSingleAllowedValuesList(): String

}


class KeyValueMatcher( val key: String, val allowedValues: List<String>): TagMatcher {

    override fun getSingleBeforeOrCurrentCondition(beforeOrCurrent: String) =
        "${this.key}_${beforeOrCurrent} in ${this.key}_tags\n"


    override fun getSingleAllowedValuesList(): String {
        val allowedValuesList = this.allowedValues
            .filter(String::isNotBlank)
            .map { "'$it'" }

        return "${allowedValuesList} as ${this.key}_tags\n,"
    }

}

