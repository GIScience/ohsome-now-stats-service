package org.heigit.ohsome.now.stats.utils

import org.heigit.ohsome.now.stats.TopicDefinition
import org.heigit.ohsome.now.stats.topics

class TopicHandler(val topic: String) {
    val definition: TopicDefinition = topics[topic]!!

    var valueLists: String = ""
    var beforeCurrent: String = ""

    init {
        buildValueLists()
        buildBeforeCurrent()
    }


    private fun buildValueLists() {
        for (matcher in definition.matchers) {
            val allowedValuesList = matcher.allowedValues
                .filter(String::isNotBlank)
                .map { "'$it'" }

            valueLists += "${allowedValuesList} as ${matcher.key}_tags\n,"
        }
    }

    private fun beforeCurrentCondition(beforeOrCurrent: String): String {
        var temp = ""
        for (matcher in definition.matchers) {
            // add "OR " between conditions
            if (temp != "") temp += "OR "

            temp += "${matcher.key}_${beforeOrCurrent} in ${matcher.key}_tags\n"
        }
        temp += "as ${beforeOrCurrent},\n"
        return temp
    }

    private fun buildBeforeCurrent() {
        beforeCurrent += beforeCurrentCondition("before")
        beforeCurrent += beforeCurrentCondition("current")
    }
}
