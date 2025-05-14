package org.heigit.ohsome.now.statsservice.topic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class TopicDefinitionTests {


    @Test
    fun `check topic aggregation strategy COUNT`() {

        val definition = TopicDefinition("amenity", listOf(KeyOnlyMatcher("amenity")), AggregationStrategy.COUNT)

        assertEquals(
            """
                ifNull(sum(edit), 0) as topic_result,
                ifNull(sum(if(edit = 1, 1, 0)), 0) as topic_result_created,
                ifNull(sum(if(edit = 0, 1, 0)), 0) as topic_result_modified,
                ifNull(sum(if(edit = -1, 1, 0)), 0) as topic_result_deleted
            """.trimIndent(),
            definition.defineTopicResult().trimIndent()
        )
    }


    @Test
    fun `check topic aggregation strategy LENTGH`() {
        // ! only mocking, not the real topic definition
        val definition = TopicDefinition("waterway", listOf(KeyOnlyMatcher("waterway")), AggregationStrategy.LENGTH)
        assertEquals(
            """
    ifNull( 
        sum( 
            multiIf(                                      -- this is a 'case'
                edit = 1, length,                        -- case, then,
                edit = 0, length_delta,                  -- case, then,
                edit = -1, - length + length_delta,     -- case, then,
                0                                         -- else
            )                                            
        )/ 1000,                                   -- m to km
        0
    ) as topic_result,
        
    ifNull(
        sum(
            if(edit = 1, length, 0)
        ) / 1000,
        0
    ) as topic_result_created,
    
    ifNull(
        abs(
            sum(
                if(edit = -1, - length + length_delta, 0)
            ) / 1000
        ),
        0
    ) as topic_result_deleted,
    
    ifNull(
        abs(
            sum(
                if(edit = 0 and length_delta < 0, length_delta, 0)
            ) / 1000
        ),
        0
    ) as topic_result_modified_less, 
    
    ifNull(
        sum(
            if(edit = 0 and length_delta > 0, length_delta, 0)
        ) / 1000,
        0
    ) as topic_result_modified_more,
    
    ifNull(
        sum(
            if(edit = 0, 1, 0)
        ),
        0
    ) as topic_result_modified
            """.trimMargin(),
            definition.defineTopicResult().trimMargin()
        )
    }

    @Test
    fun `check topic aggregation strategy AREA`() {
        // ! only mocking, not the real topic definition
        val definition = TopicDefinition("landuse", listOf(KeyOnlyMatcher("landuse")), AggregationStrategy.AREA)
        assertEquals(
            """    
    ifNull( 
        sum( 
            multiIf(                                      -- this is a 'case'
                edit = 1, area,                        -- case, then,
                edit = 0, area_delta,                  -- case, then,
                edit = -1, - area + area_delta,     -- case, then,
                0                                         -- else
            )                                            
        )/ 1000000,                                   -- m to km
        0
    ) as topic_result,
        
    ifNull(
        sum(
            if(edit = 1, area, 0)
        ) / 1000000,
        0
    ) as topic_result_created,
    
    ifNull(
        abs(
            sum(
                if(edit = -1, - area + area_delta, 0)
            ) / 1000000
        ),
        0
    ) as topic_result_deleted,
    
    ifNull(
        abs(
            sum(
                if(edit = 0 and area_delta < 0, area_delta, 0)
            ) / 1000000
        ),
        0
    ) as topic_result_modified_less, 
    
    ifNull(
        sum(
            if(edit = 0 and area_delta > 0, area_delta, 0)
        ) / 1000000,
        0
    ) as topic_result_modified_more,
    
    ifNull(
        sum(
            if(edit = 0, 1, 0)
        ),
        0
    ) as topic_result_modified
""".trimMargin(),
            definition.defineTopicResult().trimMargin()
        )
    }


    @Test
    fun `check not Value Matcher`() {
        // ! only mocking, not the real topic definition
        val definition = TopicDefinition(
            "landuse",
            listOf(KeyNotValueMatcher("landuse", listOf("trash"))),
            AggregationStrategy.COUNT
        )
        assertEquals(
            "['trash', ''] as landuse_tags,\n",
            definition.buildValueLists()
        )
        assertEquals(
            "landuse_before not in landuse_tags\n" +
                    "as before,\n",
            definition.beforeCurrentCondition(BeforeOrCurrent.BEFORE)
        )
        assertEquals(
            "landuse not in (trash)",
            definition.buildTopicDefinitionString()
        )
    }
}
