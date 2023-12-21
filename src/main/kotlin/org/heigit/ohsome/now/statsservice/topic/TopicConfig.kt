package org.heigit.ohsome.now.statsservice.topic


fun areTopicsValid(names: List<String>) = topics.keys.containsAll(names)



//TODO: make private and provide accessor by name
//TODO: avoid redundant topic name (in map key and in definition object)
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
            KeyValueMatcher("amenity", listOf("kindergarten", "school", "college", "university")),
            KeyValueMatcher("building", listOf("kindergarten", "school", "college", "university"))
        )
    ),


    "financial" to TopicDefinition(
        "financial",
        listOf(
            KeyValueMatcher(
                "amenity",
                listOf("atm", "bank", "money_transfer", "bureau_de_change", "mobile_money_agent", "payment_terminal")
            )
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


    "poi" to TopicDefinition(
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


    "social_facility" to TopicDefinition(
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


    "wash" to TopicDefinition(
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
