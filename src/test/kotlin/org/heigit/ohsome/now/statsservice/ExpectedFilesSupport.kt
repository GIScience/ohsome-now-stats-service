package org.heigit.ohsome.now.statsservice

import java.io.File
import kotlin.text.Charsets.UTF_8


fun file(name: String) = File("src/test/resources/expected_sql/$name.sql")
    .readText(UTF_8)

