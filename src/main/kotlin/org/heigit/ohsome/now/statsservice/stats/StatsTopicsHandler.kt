package org.heigit.ohsome.now.statsservice.stats


data class StatsTopicsHandler(var topics: List<String>) {

    var statsTopicsDefinition = mapOf(
        "edit" to "count(map_feature_edit) as edit",
        "contributor" to "count(distinct user_id) as contributor",
        "changeset" to "count(distinct changeset_id) as changeset"
    )

    val statsTopicSQL: String

    val statsTopicAggregationSQL: String

    val noStatsTopics: Boolean

    init {
        this.topics = topics.intersect(statsTopicsDefinition.keys).toList()
        this.noStatsTopics = topics.isEmpty()

        statsTopicSQL =
            if (this.noStatsTopics) "" else this.topics.map { statsTopicsDefinition[it] }.reduce { a, b -> "$a,\n$b" }
                .toString()

        statsTopicAggregationSQL =
            if (this.noStatsTopics) "" else this.topics.map { "groupArray($it::Float64) as $it" }
                .reduce { a, b -> "$a,\n$b" }
    }
}
