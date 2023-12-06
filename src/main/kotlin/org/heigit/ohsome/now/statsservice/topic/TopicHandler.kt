package org.heigit.ohsome.now.statsservice.topic


class TopicHandler(val topic: String) {

    val definition: TopicDefinition = topics[topic]!!

    var valueLists: String = ""
    var beforeCurrent: String = ""

    val topicResult = "ifNull(sum(edit), 0)"


    init {
        this.valueLists = this.definition.buildValueLists()
        buildBeforeCurrent()
    }


    private fun buildBeforeCurrent() {
        beforeCurrent += this.definition.beforeCurrentCondition("before")
        beforeCurrent += this.definition.beforeCurrentCondition("current")
    }

}
