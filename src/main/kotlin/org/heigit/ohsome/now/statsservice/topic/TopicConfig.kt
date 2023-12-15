package org.heigit.ohsome.now.statsservice.topic


val topics = mapOf(
    // let's try to order them alphabetically

    "amenity" to KeyOnlyTopicDefinition("amenity", "amenity"),


    "healthcare" to KeyValueTopicDefinition(
        "healthcare",
        listOf(
            KeyValueMatcher(
                "healthcare",
                listOf("doctors", "clinic", "midwife", "nurse", "center", "health_post", "hospital")
            ),
            KeyValueMatcher(
                "amenity",
                listOf("doctors", "clinic", "hospital", "health_post")
            )
        )
    ),


    "place" to KeyValueTopicDefinition(
        "place",
        listOf(
            KeyValueMatcher(
                "place",
                listOf(
                    "country",
                    "state",
                    "region",
                    "province",
                    "district",
                    "county",
                    "municipality",
                    "city",
                    "borough",
                    "suburb",
                    "quarter",
                    "neighbourhood",
                    "town",
                    "village",
                    "hamlet",
                    "isolated_dwelling"
                )
            )
        )
    ),

    // todo: amenity: social_facility also exists - include?
    "shelter" to KeyOnlyTopicDefinition("shelter", "social_facility"),


    "waterway" to KeyValueTopicDefinition(
        "waterway",
        listOf(
            KeyValueMatcher(
                "waterway",
                listOf("river", "canal", "stream", "brook", "drain", "ditch")
            )
        ),
        AggregationStrategy.LENGTH
    )
)

