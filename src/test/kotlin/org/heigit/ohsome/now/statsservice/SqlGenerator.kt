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


    private val currentSchemaVersion = schemaVersion


    private val fourHoursLater = now()
        .plus(4, HOURS)
        .truncatedTo(SECONDS)
        .toString()

    private val path = "sql_generated/"


    @Test
    fun `create topic SQL for all topics and both stages`() = getAllTopicDefinitions()
        .forEach(::writeTopicSql)


    @Test
    fun `create projections SQL for both stages`() {
        this.writeProjections("int", currentSchemaVersion)
        this.writeProjections("prod", currentSchemaVersion)
    }


    private fun writeTopicSql(definition: TopicDefinition) {
        writeDDLs(definition, "int", currentSchemaVersion)
        writeInserts(definition, "int", currentSchemaVersion)

        writeDDLs(definition, "prod", currentSchemaVersion)
        writeInserts(definition, "prod", currentSchemaVersion)
    }


    private fun writeDDLs(definition: TopicDefinition, stage: String, schemaVersion: String) =
        writeSqlToFile("DDL", title(definition), stage) {
            createDDLCommands(definition, stage, schemaVersion)
        }


    private fun writeInserts(definition: TopicDefinition, stage: String, schemaVersion: String) =
        writeSqlToFile("INSERT", title(definition), stage) {
            createInsertCommands(definition, stage, schemaVersion)
        }


    private fun writeProjections(stage: String, schemaVersion: String) =
        writeSqlToFile("PROJECTIONS", "for_stats_table", stage) {
            createProjections(stage, schemaVersion)
        }


    private fun writeSqlToFile(prefix: String, title: String, stage: String, command: () -> String) = this
        .getFile(prefix, title, stage)
        .writeText(command())


    private fun createDDLCommands(definition: TopicDefinition, stage: String, schemaVersion: String) =
        comment() +
                createTableDDL(definition, stage, schemaVersion) + "\n\n" +
                createMvDdl(definition, fourHoursLater, stage, schemaVersion)


    private fun createInsertCommands(definition: TopicDefinition, stage: String, schemaVersion: String) =
        comment() + createInsertStatement(definition, fourHoursLater, stage, schemaVersion)


    private fun createProjections(stage: String, schemaVersion: String) =
        comment() + createStatsTableProjections(stage, schemaVersion)


    private fun comment() = "-- generated by " + SqlGenerator::class.qualifiedName +
            " at " + now().toString() + "\n\n\n"


    private fun getFile(prefix: String, title: String, stage: String): File {
        val filename = stage + "/" + stage + "_" + prefix + "__" + title + ".sql"
        return File(path + filename)
    }


    private fun title(definition: TopicDefinition) = "topic_" + definition.topicName


}


