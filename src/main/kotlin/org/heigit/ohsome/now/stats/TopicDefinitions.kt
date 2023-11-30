package org.heigit.ohsome.now.stats


class TopicDefinition(
    val topic: String,
    val matchers: List<KeyValueMatcher>

)

class KeyValueMatcher(
    val key: String,
    val allowedValues: List<String>
)


val topics = mapOf(
    "place" to TopicDefinition(
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
    )
)

