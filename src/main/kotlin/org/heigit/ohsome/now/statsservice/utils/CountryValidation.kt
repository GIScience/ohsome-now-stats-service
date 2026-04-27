package org.heigit.ohsome.now.statsservice.utils


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
@Constraint(validatedBy = [ValidCountriesCheck::class])
annotation class ValidCountry(
    val message: String = "Country Code must be ISO 3166-1 alpha-3",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)


class ValidCountriesCheck : ConstraintValidator<ValidCountry, String> {

    override fun isValid(country: String, context: ConstraintValidatorContext?) = country.length == 3

}


