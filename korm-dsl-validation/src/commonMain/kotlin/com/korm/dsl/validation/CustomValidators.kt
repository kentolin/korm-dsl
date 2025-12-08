// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/validators/CustomValidators.kt

package com.korm.dsl.validation.validators

import com.korm.dsl.validation.FieldValidator
import com.korm.dsl.validation.rule

/**
 * Custom validation rules.
 */

// Custom predicate validator
fun <T, V> FieldValidator<T, V>.must(
    name: String = "custom",
    message: String = "Validation failed",
    predicate: (V) -> Boolean
): FieldValidator<T, V> {
    return addRule(rule(name, { message }, predicate))
}

// Conditional validator
fun <T, V> FieldValidator<T, V>.`when`(
    condition: (T) -> Boolean,
    message: String = "Conditional validation failed",
    predicate: (V) -> Boolean
): FieldValidator<T, V> {
    // This would require access to the parent object
    // For now, just use a simple predicate
    return addRule(rule("when", { message }, predicate))
}

// Equals validator
fun <T, V> FieldValidator<T, V>.equals(
    value: V,
    message: String = "Value must equal $value"
): FieldValidator<T, V> {
    return addRule(
        rule("equals", { message }) { it == value }
    )
}

// Not equals validator
fun <T, V> FieldValidator<T, V>.notEquals(
    value: V,
    message: String = "Value must not equal $value"
): FieldValidator<T, V> {
    return addRule(
        rule("notEquals", { message }) { it != value }
    )
}

// In validator
fun <T, V> FieldValidator<T, V>.`in`(
    vararg values: V,
    message: String = "Value must be one of: ${values.joinToString(", ")}"
): FieldValidator<T, V> {
    return addRule(
        rule("in", { message }) { it in values }
    )
}

// Not in validator
fun <T, V> FieldValidator<T, V>.notIn(
    vararg values: V,
    message: String = "Value must not be one of: ${values.joinToString(", ")}"
): FieldValidator<T, V> {
    return addRule(
        rule("notIn", { message }) { it !in values }
    )
}
