package org.heigit.ohsome.now.statsservice

import org.mockito.Mockito
import java.time.Instant


fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

fun anyInstant() = any(Instant::class.java)

