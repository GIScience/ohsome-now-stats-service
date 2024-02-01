package org.heigit.ohsome.now.statsservice.stats

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
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

