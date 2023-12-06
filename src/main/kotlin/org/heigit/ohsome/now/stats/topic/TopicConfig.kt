package org.heigit.ohsome.now.stats.topic


val topics = mapOf(

    "amenity" to KeyOnlyTopicDefinition("amenity"),

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
    )

)

