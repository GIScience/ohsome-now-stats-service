package org.heigit.ohsome.now.stats.topic


interface TopicDefinition {

    fun buildValueLists(): String
    fun beforeCurrentCondition(beforeOrCurrent: String): String
}


class KeyOnlyTopicDefinition(val topic: String) : TopicDefinition {

    override fun buildValueLists() = ""

    override fun beforeCurrentCondition(beforeOrCurrent: String) =
        " ${topic}_${beforeOrCurrent} <> '' as ${beforeOrCurrent}, "

}


class KeyValueTopicDefinition(val topic: String, val matchers: List<KeyValueMatcher>) : TopicDefinition {

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

