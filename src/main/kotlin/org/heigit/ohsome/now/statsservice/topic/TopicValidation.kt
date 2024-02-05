package org.heigit.ohsome.now.statsservice.topic

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidTopicsCheck::class])
annotation class ValidTopics(
    val message: String = "Topic not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)


class ValidTopicsCheck : ConstraintValidator<ValidTopics?, List<String>> {
    override fun isValid(topics: List<String>, context: ConstraintValidatorContext?): Boolean {
        return areTopicsValid(topics)
    }
}


