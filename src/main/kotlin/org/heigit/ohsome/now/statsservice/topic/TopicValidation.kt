package org.heigit.ohsome.now.statsservice.topic

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass


@Target(VALUE_PARAMETER, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidTopicsCheck::class])
annotation class ValidTopic(
    val message: String = "Topic not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)


class ValidTopicsCheck : ConstraintValidator<ValidTopic, String> {

    override fun isValid(topic: String, context: ConstraintValidatorContext?) = areTopicsValid(listOf(topic))

}


