package org.heigit.ohsome.now.statsservice.topic


//TODO: unit test
fun areTopicsValid(names: List<String>) = topics.keys.containsAll(names)


val topics = mapOf(
    // let"s try to order them alphabetically

    "amenity" to TopicDefinition(
        "amenity",
        listOf(KeyOnlyMatcher("amenity"))
    ),


    "healthcare" to TopicDefinition(
        "healthcare",
        listOf(
            KeyOnlyMatcher("healthcare"),
            KeyValueMatcher("amenity", listOf("doctors", "dentist", "clinic", "hospital", "pharmacy"))
        )
    ),


//    "landuse" to KeyOnlyTopicDefinition("landuse", "landuse", AggregationStrategy.AREA),


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


//    "sanitation" to KeyValueTopicDefinition(
//        "sanitation", listOf(
//            KeyValueMatcher("man_made", listOf("pumping_station", "water_tower")),
//            KeyValueMatcher("building", listOf("pumping_station")),
//            KeyValueMatcher("amenity", listOf("water_point"))
//        )
//    ),


    // todo: amenity: social_facility also exists - include?
//    "social_facility" to KeyOnlyTopicDefinition("social_facility", "social_facility"),


    "waterway" to TopicDefinition(
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
