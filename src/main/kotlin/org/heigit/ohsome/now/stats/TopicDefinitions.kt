package org.heigit.ohsome.now.stats


class TopicDefinition(

    val topic: String,

//    val matchers = listOf<AtomicTopicDefinition>()
//
    val key: String,
    val allowedValues: List<String>

)

//class AtomicTopicDefinition(
//    val key: String,
//    val allowedValues: List<String>
//)


val placeTopic = TopicDefinition(
    "place",

    "place",
    listOf( "country", "state", "region", "province", "district", "county", "municipality", "city", "borough", "suburb", "quarter",
        "neighbourhood", "town", "village", "hamlet", "isolated_dwelling")
)
val anderesTopic = TopicDefinition(
    "place",
    "place",
    listOf( "country", "state", "region", "province", "district", "county", "municipality", "city", "borough", "suburb", "quarter",
        "neighbourhood", "town", "village", "hamlet", "isolated_dwelling")
)

val topics = mapOf(
    "place" to placeTopic,
    "anderes" to anderesTopic
)

