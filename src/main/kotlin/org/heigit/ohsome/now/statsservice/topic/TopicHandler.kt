package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.BeforeOrCurrent.BEFORE
import org.heigit.ohsome.now.statsservice.topic.BeforeOrCurrent.CURRENT


class TopicHandler(val topic: String) {

    val definition = getTopicDefinitionByName(topic)


    fun valueLists() = this.definition.buildValueLists()
    fun topicResult() = this.definition.defineTopicResult()

    fun beforeCurrent() = this.definition.beforeCurrentCondition(BEFORE) + this.definition.beforeCurrentCondition(CURRENT)


}
