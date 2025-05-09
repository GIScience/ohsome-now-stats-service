package org.heigit.ohsome.now.statsservice

import org.heigit.ohsome.now.statsservice.topic.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant.now
import java.time.temporal.ChronoUnit.HOURS
import java.time.temporal.ChronoUnit.SECONDS


@Disabled("Run manually only for schema changes")
class SqlGenerator {


    private val currentStatsSchemaVersion = statsSchemaVersion
    private val currentTopicSchemaVersion = topicSchemaVersion

//    private val fourHoursLater = "2025-05-05T00:00:00Z"
    private val fourHoursLater = now()
        .plus(4, HOURS)
        .truncatedTo(SECONDS)
        .toString()

    private val path = "sql_generated/"


    @Test
    fun `create stats main table SQL for both stages`() {
        writeStatsSql("int")
        writeStatsSql("prod")
    }


    @Test
    fun `create stats projection SQL for both stages`() {
        this.writeProjections("int")
        this.writeProjections("prod")
    }


    @Test
    fun `create stats MV SQL for both stages`() {
        this.writeStatsMVSQL("int")
        this.writeStatsMVSQL("prod")
    }


    @Test
    fun `create topic SQL for all topics and both stages`() = getAllTopicDefinitions()
        .forEach(::writeTopicSql)


    private fun writeStatsSql(stage: String) {
        writeSqlToFile("DDL", "stats_table", stage) {
            createStatsTableDDLCommands(stage, currentStatsSchemaVersion)
        }
    }



//    @Test
//    @Disabled
//    fun `create topic DELETION-SQL for all topics and both stages`() = getAllTopicDefinitions()
//        .forEach(::writeTopicDeleteSql)


    private fun writeTopicDeleteSql(definition: TopicDefinition) {
        writeDeletes(definition, "int", currentTopicSchemaVersion)
        writeDeletes(definition, "prod", currentTopicSchemaVersion)
    }


    private fun writeTopicSql(definition: TopicDefinition) {
        writeDDLs(definition, "int", currentStatsSchemaVersion, currentTopicSchemaVersion)
        writeInserts(definition, "int", currentStatsSchemaVersion, currentTopicSchemaVersion)

        writeDDLs(definition, "prod", currentStatsSchemaVersion, currentTopicSchemaVersion)
        writeInserts(definition, "prod", currentStatsSchemaVersion, currentTopicSchemaVersion)
    }


    private fun writeDDLs(definition: TopicDefinition, stage: String, statsSchemaVersion: String, topicSchemaVersion: String) =
        writeSqlToFile("DDL", title(definition), stage) {
            createDDLCommands(definition, stage, statsSchemaVersion, topicSchemaVersion)
        }


    private fun writeInserts(definition: TopicDefinition, stage: String, statsSchemaVersion: String, topicSchemaVersion: String) =
        writeSqlToFile("INSERT", title(definition), stage) {
            createInsertCommands(definition, stage, statsSchemaVersion, topicSchemaVersion)
        }

    private fun writeDeletes(definition: TopicDefinition, stage: String, topicSchemaVersion: String) =
        writeSqlToFile("DELETE", title(definition), stage) {
            createDeleteCommands(definition, stage, topicSchemaVersion)
        }

    private fun writeProjections(stage: String) =
        writeSqlToFile("PROJECTIONS", "for_stats_table", stage) {
            createProjections(stage, currentStatsSchemaVersion)
        }


    private fun writeStatsMVSQL(stage: String) {
        writeSqlToFile("DDL", "hashtag_aggregation_for_stats_table", stage) {
            createStatsMVDDL(stage, currentStatsSchemaVersion)
        }

    }


    private fun writeSqlToFile(prefix: String, title: String, stage: String, command: () -> String) = this
        .getFile(prefix, title, stage)
        .writeText(command())

    private fun createStatsTableDDLCommands(stage: String, statsSchemaVersion: String) =
        comment() + createStatsTableDDL(stage, statsSchemaVersion)

    private fun createDDLCommands(definition: TopicDefinition, stage: String, statsSchemaVersion: String, topicSchemaVersion: String) =
        comment() +
                createTableDDL(definition, stage, topicSchemaVersion) + "\n\n" +
                createMvDdl(definition, fourHoursLater, stage, statsSchemaVersion, topicSchemaVersion)


    private fun createInsertCommands(definition: TopicDefinition, stage: String, statsSchemaVersion: String, topicSchemaVersion: String) =
        comment() + createInsertStatement(definition, fourHoursLater, stage, statsSchemaVersion, topicSchemaVersion)

    private fun createDeleteCommands(definition: TopicDefinition, stage: String, topicSchemaVersion: String) =
        comment() + createDeleteStatement(definition, fourHoursLater, stage, topicSchemaVersion)

    private fun createProjections(stage: String, statsSchemaVersion: String) =
        comment() + createStatsTableProjections(stage, statsSchemaVersion)


    private fun createStatsMVDDL(stage: String, statsSchemaVersion: String) =
        comment() +
                createStatsTableMaterializedViewForHashtagAggregation(stage, statsSchemaVersion) + "\n\n" +
                createStatsMaterializedViewForHashtagAggregation(stage, statsSchemaVersion)


    private fun comment() = "-- generated by " + SqlGenerator::class.qualifiedName +
            " at " + now().toString() + "\n\n\n"


    private fun getFile(prefix: String, title: String, stage: String): File {
        val filename = stage + "/" + stage + "_" + prefix + "__" + title + ".sql"
        return File(path + filename)
    }


    private fun title(definition: TopicDefinition) = "topic_" + definition.topicName


}


