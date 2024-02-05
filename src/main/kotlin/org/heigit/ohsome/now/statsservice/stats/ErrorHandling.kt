package org.heigit.ohsome.now.statsservice.stats

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus


// see https://reflectoring.io/bean-validation-with-spring-boot/#handling-validation-errors
//class ValidationErrorResponse(val violations: List<Violation>)

class Violation(
    val message: String,
    val invalidValue: String
)

@ControllerAdvice
internal class ErrorHandlingControllerAdvice {

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    fun onConstraintValidationException(validationError: ConstraintViolationException) = validationError
        .constraintViolations
        .map { Violation(it.message, it.invalidValue.toString()) }
//        .let { ValidationErrorResponse(it) }

}


