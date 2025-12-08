// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/Validator.kt

package com.korm.dsl.validation

import kotlin.reflect.KProperty1

/**
 * Base interface for validators.
 */
interface Validator<T> {
    /**
     * Validate an object.
     */
    fun validate(value: T): ValidationResult

    /**
     * Check if value is valid.
     */
    fun isValid(value: T): Boolean {
        return validate(value).isValid
    }
}

/**
 * Validation result.
 */
data class ValidationResult(
    val isValid: Boolean,
    val violations: List<ValidationViolation> = emptyList()
) {
    /**
     * Get violation messages.
     */
    fun getMessages(): List<String> {
        return violations.map { it.message }
    }

    /**
     * Get violations for a specific field.
     */
    fun getViolationsForField(field: String): List<ValidationViolation> {
        return violations.filter { it.field == field }
    }

    /**
     * Combine with another validation result.
     */
    operator fun plus(other: ValidationResult): ValidationResult {
        return ValidationResult(
            isValid = isValid && other.isValid,
            violations = violations + other.violations
        )
    }

    companion object {
        fun valid(): ValidationResult = ValidationResult(true)

        fun invalid(violations: List<ValidationViolation>): ValidationResult =
            ValidationResult(false, violations)

        fun invalid(violation: ValidationViolation): ValidationResult =
            ValidationResult(false, listOf(violation))
    }
}

/**
 * Validation violation.
 */
data class ValidationViolation(
    val field: String,
    val message: String,
    val invalidValue: Any? = null,
    val constraint: String? = null
)

/**
 * Validation exception.
 */
class ValidationException(
    val result: ValidationResult
) : RuntimeException(result.getMessages().joinToString(", ")) {

    constructor(violations: List<ValidationViolation>) : this(ValidationResult.invalid(violations))

    constructor(violation: ValidationViolation) : this(ValidationResult.invalid(violation))
}

/**
 * Throw validation exception if result is invalid.
 */
fun ValidationResult.orThrow() {
    if (!isValid) {
        throw ValidationException(this)
    }
}
