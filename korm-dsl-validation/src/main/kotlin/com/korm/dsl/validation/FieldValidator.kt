// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/FieldValidator.kt

package com.korm.dsl.validation

import kotlin.reflect.KProperty1

/**
 * Validator for a specific field.
 */
class FieldValidator<T, V>(
    private val property: KProperty1<T, V>,
    private val rules: MutableList<ValidationRule<V>> = mutableListOf()
) {

    /**
     * Add a validation rule.
     */
    fun addRule(rule: ValidationRule<V>): FieldValidator<T, V> {
        rules.add(rule)
        return this
    }

    /**
     * Validate field value.
     */
    fun validate(obj: T): List<ValidationViolation> {
        val value = property.get(obj)
        val violations = mutableListOf<ValidationViolation>()

        for (rule in rules) {
            if (!rule.predicate(value)) {
                violations.add(
                    ValidationViolation(
                        field = property.name,
                        message = rule.message(value),
                        invalidValue = value,
                        constraint = rule.name
                    )
                )
            }
        }

        return violations
    }
}

/**
 * Validation rule.
 */
data class ValidationRule<V>(
    val name: String,
    val predicate: (V) -> Boolean,
    val message: (V) -> String
)

/**
 * Create a validation rule.
 */
fun <V> rule(
    name: String,
    message: (V) -> String = { "Validation failed for constraint: $name" },
    predicate: (V) -> Boolean
): ValidationRule<V> {
    return ValidationRule(name, predicate, message)
}
