package org.heigit.ohsome.stats

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StatsApplication

fun main(args: Array<String>) {
	runApplication<StatsApplication>(*args)
}
