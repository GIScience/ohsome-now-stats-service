package org.heigit.ohsome.now.stats


class TopicDefinition(val topic: String, val matchers: List<KeyValueMatcher>) {

    fun buildValueLists(): String {
        var valueLists = ""

        for (matcher in this.matchers) {
            val allowedValuesList = matcher.allowedValues
                .filter(String::isNotBlank)
                .map { "'$it'" }

            valueLists += "${allowedValuesList} as ${matcher.key}_tags\n,"
        }

        return valueLists
    }


    fun beforeCurrentCondition(beforeOrCurrent: String): String {
        var temp = ""
        for (matcher in this.matchers) {
            // add "OR " between conditions
            if (temp != "") temp += "OR "

            temp += "${matcher.key}_${beforeOrCurrent} in ${matcher.key}_tags\n"
        }
        temp += "as ${beforeOrCurrent},\n"
        return temp
    }

}


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
    ),
    "healthcare" to TopicDefinition(
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
    //TODO: fix this
    "amenity" to TopicDefinition(
        "amenity",
        listOf(
            KeyValueMatcher(
                "amenity",
                listOf("XXXXXXXXXXXXXXXX")
            )
        )
    )
)

