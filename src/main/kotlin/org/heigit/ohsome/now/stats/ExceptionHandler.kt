package org.heigit.ohsome.now.stats

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus


data class UnparsableISO8601StringException(override val message: String? = null) : Exception(message)
data class ISO8601TooSmallException(override val message: String? = null) : Exception(message)

@ControllerAdvice
class GlobalExceptionHandler {
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid ISO8601 string as interval!")
    @ExceptionHandler(UnparsableISO8601StringException::class)
    fun handleUnparsableISO8601StringException(exception: UnparsableISO8601StringException) {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Interval must not be under 1 minute!")
    @ExceptionHandler(ISO8601TooSmallException::class)
    fun handleISO8601TooSmallException(exception: ISO8601TooSmallException) {
    }
}