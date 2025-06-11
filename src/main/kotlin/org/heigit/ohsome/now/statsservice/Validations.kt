package org.heigit.ohsome.now.statsservice

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.heigit.ohsome.now.statsservice.utils.isLessThanOneMinute
import org.heigit.ohsome.now.statsservice.utils.isParseableISO8601String
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass


@Target(VALUE_PARAMETER, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [HashtagValidator::class])
annotation class ValidHashtag(
    val message: String = "Hashtag must not be '*'",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class HashtagValidator : ConstraintValidator<ValidHashtag, String> {

    override fun isValid(hashtag: String, context: ConstraintValidatorContext?) =
        hashtag != "*"

}

@Target(VALUE_PARAMETER, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ResolutionValidator::class])
annotation class ValidResolution(
    val message: String = "Currently only resolution 3 and 6 supported",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ResolutionValidator : ConstraintValidator<ValidResolution, Int> {

    override fun isValid(resolution: Int, context: ConstraintValidatorContext?) =
        intArrayOf(3, 6).contains(resolution)

}


@Target(VALUE_PARAMETER, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ISO8601StringIntervalValidator::class])
annotation class ParseableInterval(
    val message: String = "Invalid ISO8601 string as interval.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ISO8601StringIntervalValidator : ConstraintValidator<ParseableInterval, String> {

    override fun isValid(interval: String, context: ConstraintValidatorContext?) = isParseableISO8601String(interval)

}


@Target(VALUE_PARAMETER, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [AtLeastOneMinuteIntervalValidator::class])
annotation class AtLeastOneMinuteInterval(
    val message: String = "Interval must not be under 1 minute.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class AtLeastOneMinuteIntervalValidator : ConstraintValidator<AtLeastOneMinuteInterval, String> {

    override fun isValid(interval: String, context: ConstraintValidatorContext?) = !isLessThanOneMinute(interval)

}

