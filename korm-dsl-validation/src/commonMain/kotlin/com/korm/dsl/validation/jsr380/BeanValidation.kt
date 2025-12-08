// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/jsr380/BeanValidation.kt

package com.korm.dsl.validation.jsr380

import com.korm.dsl.validation.ValidationResult
import com.korm.dsl.validation.ValidationViolation
import com.korm.dsl.validation.Validator
import jakarta.validation.Validation
import jakarta.validation.Validator as JakartaValidator
import jakarta.validation.ConstraintViolation

/**
 * JSR-380 Bean Validation integration.
 */
class BeanValidator<T : Any> : Validator<T> {

    private val validator: JakartaValidator = Validation
        .buildDefaultValidatorFactory()
        .validator

    override fun validate(value: T): ValidationResult {
        val violations = validator.validate(value)

        if (violations.isEmpty()) {
            return ValidationResult.valid()
        }

        val validationViolations = violations.map { it.toValidationViolation() }
        return ValidationResult.invalid(validationViolations)
    }

    /**
     * Validate specific groups.
     */
    fun validate(value: T, vararg groups: Class<*>): ValidationResult {
        val violations = validator.validate(value, *groups)

        if (violations.isEmpty()) {
            return ValidationResult.valid()
        }

        val validationViolations = violations.map { it.toValidationViolation() }
        return ValidationResult.invalid(validationViolations)
    }

    /**
     * Validate a specific property.
     */
    fun validateProperty(value: T, propertyName: String): ValidationResult {
        val violations = validator.validateProperty(value, propertyName)

        if (violations.isEmpty()) {
            return ValidationResult.valid()
        }

        val validationViolations = violations.map { it.toValidationViolation() }
        return ValidationResult.invalid(validationViolations)
    }

    private fun ConstraintViolation<T>.toValidationViolation(): ValidationViolation {
        return ValidationViolation(
            field = propertyPath.toString(),
            message = message,
            invalidValue = invalidValue,
            constraint = constraintDescriptor.annotation.annotationClass.simpleName
        )
    }
}

/**
 * Create a Bean Validator.
 */
fun <T : Any> beanValidator(): BeanValidator<T> {
    return BeanValidator()
}
