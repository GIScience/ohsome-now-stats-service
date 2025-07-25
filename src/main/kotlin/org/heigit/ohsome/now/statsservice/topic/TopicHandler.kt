package org.heigit.ohsome.now.statsservice.topic

import org.heigit.ohsome.now.statsservice.topic.BeforeOrCurrent.BEFORE
import org.heigit.ohsome.now.statsservice.topic.BeforeOrCurrent.CURRENT


data class TopicHandler(val topic: String) {

    val definition = getTopicDefinitionByName(topic)


    fun valueLists() = this.definition.buildValueLists()
    fun topicResult() = this.definition.defineTopicResult()
    fun topicMainResult() = this.definition.defineTopicMainResult()
    fun topicArrayResult() = this.definition.defineTopicArrayResult()
    fun beforeCurrent() =
        this.definition.beforeCurrentCondition(BEFORE) + this.definition.beforeCurrentCondition(CURRENT)


}
