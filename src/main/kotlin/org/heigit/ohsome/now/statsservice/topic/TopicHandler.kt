package org.heigit.ohsome.now.statsservice.topic


class TopicHandler(val topic: String) {

    val definition: TopicDefinition = topics[topic]!!

    var beforeCurrent: String = ""
    var valueLists = this.definition.buildValueLists()
    var topicResult = this.definition.defineTopicResult()


    init {
        buildBeforeCurrent()
    }


    private fun buildBeforeCurrent() {
        beforeCurrent += this.definition.beforeCurrentCondition("before")
        beforeCurrent += this.definition.beforeCurrentCondition("current")
    }

}
