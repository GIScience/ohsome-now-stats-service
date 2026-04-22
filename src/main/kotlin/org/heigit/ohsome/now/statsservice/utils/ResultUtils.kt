package org.heigit.ohsome.now.statsservice.utils

fun Map<String, Any>.getSqlArray(key: String): Array<*>? {
    if (this[key] == null) return null
    return ((this[key] as java.sql.Array).array as Array<*>)
}

fun Map<String, Any>.getDoubleArray(key: String): DoubleArray? =
    getSqlArray(key)?.map { (it as Number).toDouble() }?.toDoubleArray()

fun Map<String, Any>.toLongArray(key: String): LongArray? =
    getSqlArray(key)?.map { (it as Number).toLong() }?.toLongArray()