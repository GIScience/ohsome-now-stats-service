package org.heigit.ohsome.now.statsservice.topic


fun getAllTopicDefinitions() = validDefinitions()

fun getTopicDefinitionByName(name: String) = validDefinitions()
    .find { it.topicName == name }!!

val statsTopics = mapOf(
    "contributor" to "The number of distinct users that edited OSM data",
    "edit" to "The number of all OSM edits to \"map features\"",
    "changeset" to "The number of changesets containing the OSM edits to \"map features\""
)

fun areTopicsValid(names: List<String>) = (
        statsTopics.keys
                + validDefinitions().map(TopicDefinition::topicName)
        ).containsAll(names)


private fun validDefinitions() = topics
    .also(::assertUniqueTopicsNames)


// let's try to order them alphabetically
private val topics = listOf(

    TopicDefinition(
        "amenity",
        listOf(KeyOnlyMatcher("amenity"))
    ),


    TopicDefinition(
        "body_of_water",
        listOf(KeyValueMatcher("water", listOf("pond", "lake", "reservoir", "basin", "oxbow", "lagoon", "fishpond")))
    ),


    TopicDefinition(
        "building",
        listOf(KeyNotValueMatcher("building", listOf("no")))
    ),


    TopicDefinition(
        "commercial",
        listOf(KeyOnlyMatcher("shop"))
    ),


    TopicDefinition(
        "education",
        listOf(
            KeyValueMatcher("amenity", listOf("kindergarten", "school", "college", "university")),
            KeyValueMatcher("building", listOf("kindergarten", "school", "college", "university"))
        )
    ),


    TopicDefinition(
        "financial",
        listOf(
            KeyValueMatcher(
                "amenity",
                listOf("atm", "bank", "money_transfer", "bureau_de_change", "mobile_money_agent", "payment_terminal")
            )
        )
    ),


    TopicDefinition(
        "healthcare",
        listOf(
            KeyOnlyMatcher("healthcare"),
            KeyValueMatcher("amenity", listOf("doctors", "dentist", "clinic", "hospital", "pharmacy"))
        )
    ),


    TopicDefinition(
        "lulc",
        listOf(
            KeyOnlyMatcher("landuse"),
            KeyValueMatcher(
                "natural", listOf(
                    "bare_rock",
                    "beach",
                    "dune",
                    "fell",
                    "glacier",
                    "grassland",
                    "heath",
                    "landslide",
                    "mud",
                    "rock",
                    "sand",
                    "scree",
                    "scrub",
                    "shingle",
                    "water",
                    "wetland",
                    "wood"
                )
            ),
            KeyValueMatcher(
                "waterway", listOf(
                    "boatyard",
                    "dam",
                    "dock",
                    "riverbank"
                )
            )
        ),
        AggregationStrategy.AREA
    ),


    TopicDefinition(
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


    TopicDefinition(
        "poi",
        listOf(
            KeyOnlyMatcher("amenity"),
            KeyOnlyMatcher("shop"),
            KeyOnlyMatcher("craft"),
            KeyOnlyMatcher("office"),
            KeyOnlyMatcher("leisure"),
            KeyOnlyMatcher("aeroway")
        )
    ),


    TopicDefinition(
        "power",
        listOf(
            KeyOnlyMatcher("power"),
        )
    ),


    // TODO: DEPRECATED!!! USE ROAD INSTEAD
    TopicDefinition(
        "highway",
        listOf(
            KeyOnlyMatcher("highway")
        ),
        AggregationStrategy.LENGTH
    ),


    TopicDefinition(
        "road",
        listOf(
            KeyOnlyMatcher("highway")
        ),
        AggregationStrategy.LENGTH
    ),


    TopicDefinition(
        "social_facility",
        listOf(
            KeyOnlyMatcher("social_facility"),
            KeyValueMatcher(
                "amenity",
                listOf(
                    "shelter",
                    "social_facility",
                    "refugee_site",
                )
            )
        )
    ),


    TopicDefinition(
        "wash",
        listOf(
            KeyValueMatcher(
                "amenity", listOf(
                    "toilets",
                    "shower",
                    "drinking_water",
                    "water_point"
                )
            ),
            KeyValueMatcher(
                "man_made", listOf(
                    "water_tap",
                    "borehole",
                    "water_works",
                    "pumping_station",
                    "pump",
                    "wastewater_plant",
                    "storage_tank",
                    "water_well",
                    "water_tower",
                    "reservoir_covered",
                    "water_tank"
                )
            )
        )
    ),


    TopicDefinition(
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


fun buildTopicDefinitionMap(): Map<String, String> {
    val topicDefinitionsMap = mutableMapOf<String, String>()
    topics.forEach { topicDefinitionsMap[it.topicName] = it.buildTopicDefinitionString() }
    return statsTopics + topicDefinitionsMap
}


private fun assertUniqueTopicsNames(definitions: List<TopicDefinition>) {

    val uniqueCount = definitions
        .map(TopicDefinition::topicName)
        .distinct()
        .size

    assert(definitions.size == uniqueCount) { "ERROR: topic names are not unique - please check the topic config!" }
}
