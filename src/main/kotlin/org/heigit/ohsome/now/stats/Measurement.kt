package org.heigit.ohsome.now.stats

import kotlin.system.measureTimeMillis


//TODO: rename payload to result
class Measured<T>(val payload: T, val executionTime: Long)

fun <T> measure(command: () -> T): Measured<T> {
    val result: T
    val executionTime = measureTimeMillis {
        result = command.invoke()
    }

    return Measured(result, executionTime)
}

