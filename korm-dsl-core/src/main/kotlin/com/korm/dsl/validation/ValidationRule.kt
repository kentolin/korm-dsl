package com.korm.dsl.validation

/**
 * Result of a validation
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult() {
        constructor(vararg errors: String) : this(errors.toList())
    }

    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
}

/**
 * Base validation rule
 */
interface ValidationRule<T> {
    val fieldName: String
    val message: String?

    fun validate(value: T?): ValidationResult
}

/**
 * Composite validator that can hold multiple rules
 */
class Validator<T> {
    private val rules = mutableListOf<ValidationRule<T>>()

    fun addRule(rule: ValidationRule<T>): Validator<T> {
        rules.add(rule)
        return this
    }

    fun validate(value: T?): ValidationResult {
        val errors = mutableListOf<String>()

        rules.forEach { rule ->
            val result = rule.validate(value)
            if (result is ValidationResult.Invalid) {
                errors.addAll(result.errors)
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

/**
 * Validation context for validating entities
 */
class ValidationContext {
    private val errors = mutableMapOf<String, MutableList<String>>()

    fun <T> validate(fieldName: String, value: T?, validator: Validator<T>) {
        val result = validator.validate(value)
        if (result is ValidationResult.Invalid) {
            errors.getOrPut(fieldName) { mutableListOf() }.addAll(result.errors)
        }
    }

    fun isValid(): Boolean = errors.isEmpty()

    fun getErrors(): Map<String, List<String>> = errors

    fun getErrorMessages(): List<String> {
        return errors.flatMap { (field, messages) ->
            messages.map { "$field: $it" }
        }
    }

    fun throwIfInvalid() {
        if (!isValid()) {
            throw ValidationException(getErrorMessages())
        }
    }
}

/**
 * Validation exception
 */
class ValidationException(val errors: List<String>) : Exception(
    "Validation failed:\n${errors.joinToString("\n")}"
)