// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/EntityValidator.kt

package com.korm.dsl.validation

import kotlin.reflect.KProperty1

/**
 * Validator for entities.
 */
class EntityValidator<T : Any> : Validator<T> {
    private val fieldValidators = mutableMapOf<String, FieldValidator<T, *>>()
    private val customValidators = mutableListOf<(T) -> ValidationResult>()

    /**
     * Add field validator.
     */
    fun <V> field(
        property: KProperty1<T, V>,
        block: FieldValidator<T, V>.() -> Unit
    ): EntityValidator<T> {
        val validator = FieldValidator(property)
        validator.block()
        fieldValidators[property.name] = validator
        return this
    }

    /**
     * Add custom validation logic.
     */
    fun custom(validator: (T) -> ValidationResult): EntityValidator<T> {
        customValidators.add(validator)
        return this
    }

    /**
     * Add custom validation with violation builder.
     */
    fun custom(block: (T) -> List<ValidationViolation>): EntityValidator<T> {
        customValidators.add { entity ->
            val violations = block(entity)
            if (violations.isEmpty()) {
                ValidationResult.valid()
            } else {
                ValidationResult.invalid(violations)
            }
        }
        return this
    }

    override fun validate(value: T): ValidationResult {
        val violations = mutableListOf<ValidationViolation>()

        // Validate fields
        fieldValidators.values.forEach { validator ->
            violations.addAll(validator.validate(value))
        }

        // Run custom validators
        customValidators.forEach { validator ->
            val result = validator(value)
            violations.addAll(result.violations)
        }

        return if (violations.isEmpty()) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(violations)
        }
    }
}

/**
 * Create an entity validator.
 */
fun <T : Any> validator(block: EntityValidator<T>.() -> Unit): EntityValidator<T> {
    return EntityValidator<T>().apply(block)
}
