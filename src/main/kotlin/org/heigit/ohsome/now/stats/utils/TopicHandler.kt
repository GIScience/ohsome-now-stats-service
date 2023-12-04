package org.heigit.ohsome.now.stats.utils

import org.heigit.ohsome.now.stats.TopicDefinition
import org.heigit.ohsome.now.stats.topics


class TopicHandler(val topic: String) {

    val definition: TopicDefinition = topics[topic]!!

    var valueLists: String = ""
    var beforeCurrent: String = ""

    init {
        this.valueLists = this.definition.buildValueLists()
        buildBeforeCurrent()
    }


    private fun buildBeforeCurrent() {
        beforeCurrent += this.definition.beforeCurrentCondition("before")
        beforeCurrent += this.definition.beforeCurrentCondition("current")
    }

}
