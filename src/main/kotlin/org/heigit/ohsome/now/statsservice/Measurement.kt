package org.heigit.ohsome.now.statsservice

import kotlin.system.measureTimeMillis


class Measured<T>(val result: T, val executionTime: Long)

fun <T> measure(command: () -> T): Measured<T> {
    val result: T
    val executionTime = measureTimeMillis {
        result = command.invoke()
    }

    return Measured(result, executionTime)
}

