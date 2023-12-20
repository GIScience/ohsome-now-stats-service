package org.heigit.ohsome.now.statsservice.topic


//TODO: unit test
fun areTopicsValid(names: List<String>) = topics.keys.containsAll(names)


val topics = mapOf(
    // let's try to order them alphabetically

    "amenity" to TopicDefinition(
        "amenity",
        listOf(KeyOnlyMatcher("amenity"))
    ),


    "commercial" to TopicDefinition(
        "commercial",
        listOf(KeyOnlyMatcher("shop"))
    ),


    "education" to TopicDefinition(
        "education",
        listOf(
            KeyValueMatcher("amenity",  listOf("kindergarten", "school", "college", "university")),
            KeyValueMatcher("building", listOf("kindergarten", "school", "college", "university"))
        )
    ),


    "healthcare" to TopicDefinition(
        "healthcare",
        listOf(
            KeyOnlyMatcher("healthcare"),
            KeyValueMatcher("amenity", listOf("doctors", "dentist", "clinic", "hospital", "pharmacy"))
        )
    ),


    "lulc" to TopicDefinition(
        "landuse",
        listOf(
            KeyOnlyMatcher("landuse"),
            KeyValueMatcher("natural", listOf(
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
            )),
            KeyValueMatcher("waterway", listOf(
                "boatyard",
                "dam",
                "dock",
                "riverbank"
            ))
        ),
        AggregationStrategy.AREA
    ),


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


    "social_facility" to TopicDefinition("social_facility",
        listOf(
            KeyOnlyMatcher("social_facility"),
            KeyValueMatcher("amenity",
            listOf(
                "shelter",
                "social_facility",
                "refugee_site",
            )
        )
    )),


    "wash" to TopicDefinition(
        "wash",
        listOf(
            KeyValueMatcher("amenity",  listOf(
                "toilets",
                "shower",
                "drinking_water",
                "water_point"
            )),
            KeyValueMatcher("man_made", listOf(
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
            ))
        )
    ),


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
