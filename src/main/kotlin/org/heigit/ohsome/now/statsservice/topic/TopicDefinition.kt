package org.heigit.ohsome.now.statsservice.topic

@Suppress("LongMethod")
fun lengthOrAreaAggregation(type: String, divideBy: Int): String {
    return """        
    ifNull( 
        sum( 
            multiIf(                                      -- this is a 'case'
                edit = 1, ${type},                        -- case, then,
                edit = 0, ${type}_delta,                  -- case, then,
                edit = -1, - ${type} + ${type}_delta,     -- case, then,
                0                                         -- else
            )                                            
        )/ ${divideBy},                                   -- m to km
        0
    ) as topic_result,
        
    ifNull(
        sum(
            if(edit = 1, ${type}, 0)
        ) / ${divideBy},
        0
    ) as topic_result_created,
    
    ifNull(
        abs(
            sum(
                if(edit = -1, - ${type} + ${type}_delta, 0)
            ) / ${divideBy}
        ),
        0
    ) as topic_result_deleted,
    
    ifNull(
        abs(
            sum(
                if(edit = 0 and ${type}_delta < 0, ${type}_delta, 0)
            ) / ${divideBy}
        ),
        0
    ) as topic_result_modified_less, 
    
    ifNull(
        sum(
            if(edit = 0 and ${type}_delta > 0, ${type}_delta, 0)
        ) / ${divideBy},
        0
    ) as topic_result_modified_more,
    
    ifNull(
        sum(
            if(edit = 0, 1, 0)
        ),
        0
    ) as topic_result_modified
    """
}

const val arraySQLBase = """
    groupArray(toFloat64(topic_result)) as topic_result,
    groupArray(toFloat64(topic_result_created)) as topic_result_created,
    groupArray(topic_result_modified) as topic_result_modified,
    groupArray(toFloat64(topic_result_deleted)) as topic_result_deleted,
"""
const val arraySQLDetail = """
    groupArray(topic_result_modified_more) as topic_result_modified_more,
    groupArray(topic_result_modified_less) as topic_result_modified_less,
"""

enum class AggregationStrategy(val sql: String, val arraySql: String) {
    COUNT(
        """
            ifNull(sum(edit), 0) as topic_result,
            ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
            ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
            ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted
        """,
        arraySQLBase
    ),
    LENGTH(
        lengthOrAreaAggregation("length", 1000),
        arraySQLBase + arraySQLDetail
    ),
    AREA(
        lengthOrAreaAggregation("area", 1000000),
        arraySQLBase + arraySQLDetail
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

    fun defineTopicArrayResult() = aggregationStrategy.arraySql


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

