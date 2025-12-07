// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/validators/CommonValidators.kt

package com.korm.dsl.validation.validators

import com.korm.dsl.validation.FieldValidator
import com.korm.dsl.validation.ValidationRule
import com.korm.dsl.validation.rule

/**
 * Common validation rules for fields.
 */

// Not null validator
fun <T, V> FieldValidator<T, V?>.notNull(message: String = "Field must not be null"): FieldValidator<T, V?> {
    return addRule(
        rule("notNull", { message }) { it != null }
    )
}

// Not blank validator (for strings)
fun <T> FieldValidator<T, String?>.notBlank(message: String = "Field must not be blank"): FieldValidator<T, String?> {
    return addRule(
        rule("notBlank", { message }) { !it.isNullOrBlank() }
    )
}

// Not empty validator (for strings)
fun <T> FieldValidator<T, String?>.notEmpty(message: String = "Field must not be empty"): FieldValidator<T, String?> {
    return addRule(
        rule("notEmpty", { message }) { !it.isNullOrEmpty() }
    )
}

// Size validators
fun <T> FieldValidator<T, String?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidator<T, String?> {
    return addRule(
        rule(
            "size",
            { value ->
                message ?: buildString {
                    append("String length must be")
                    if (min != null) append(" at least $min")
                    if (min != null && max != null) append(" and")
                    if (max != null) append(" at most $max")
                    append(", but was ${value?.length ?: 0}")
                }
            }
        ) { value ->
            if (value == null) return@rule true
            val length = value.length
            (min == null || length >= min) && (max == null || length <= max)
        }
    )
}

fun <T, C : Collection<*>> FieldValidator<T, C?>.size(
    min: Int? = null,
    max: Int? = null,
    message: String? = null
): FieldValidator<T, C?> {
    return addRule(
        rule(
            "size",
            { value ->
                message ?: buildString {
                    append("Collection size must be")
                    if (min != null) append(" at least $min")
                    if (min != null && max != null) append(" and")
                    if (max != null) append(" at most $max")
                    append(", but was ${value?.size ?: 0}")
                }
            }
        ) { value ->
            if (value == null) return@rule true
            val size = value.size
            (min == null || size >= min) && (max == null || size <= max)
        }
    )
}

// Min/Max validators for numbers
fun <T> FieldValidator<T, Int?>.min(
    value: Int,
    message: String = "Value must be at least $value"
): FieldValidator<T, Int?> {
    return addRule(
        rule("min", { message }) { it == null || it >= value }
    )
}

fun <T> FieldValidator<T, Int?>.max(
    value: Int,
    message: String = "Value must be at most $value"
): FieldValidator<T, Int?> {
    return addRule(
        rule("max", { message }) { it == null || it <= value }
    )
}

fun <T> FieldValidator<T, Long?>.min(
    value: Long,
    message: String = "Value must be at least $value"
): FieldValidator<T, Long?> {
    return addRule(
        rule("min", { message }) { it == null || it >= value }
    )
}

fun <T> FieldValidator<T, Long?>.max(
    value: Long,
    message: String = "Value must be at most $value"
): FieldValidator<T, Long?> {
    return addRule(
        rule("max", { message }) { it == null || it <= value }
    )
}

fun <T> FieldValidator<T, Double?>.min(
    value: Double,
    message: String = "Value must be at least $value"
): FieldValidator<T, Double?> {
    return addRule(
        rule("min", { message }) { it == null || it >= value }
    )
}

fun <T> FieldValidator<T, Double?>.max(
    value: Double,
    message: String = "Value must be at most $value"
): FieldValidator<T, Double?> {
    return addRule(
        rule("max", { message }) { it == null || it <= value }
    )
}

// Range validators
fun <T> FieldValidator<T, Int?>.range(
    min: Int,
    max: Int,
    message: String = "Value must be between $min and $max"
): FieldValidator<T, Int?> {
    return addRule(
        rule("range", { message }) { it == null || it in min..max }
    )
}

fun <T> FieldValidator<T, Long?>.range(
    min: Long,
    max: Long,
    message: String = "Value must be between $min and $max"
): FieldValidator<T, Long?> {
    return addRule(
        rule("range", { message }) { it == null || it in min..max }
    )
}

// Positive/Negative validators
fun <T> FieldValidator<T, Int?>.positive(
    message: String = "Value must be positive"
): FieldValidator<T, Int?> {
    return addRule(
        rule("positive", { message }) { it == null || it > 0 }
    )
}

fun <T> FieldValidator<T, Int?>.negative(
    message: String = "Value must be negative"
): FieldValidator<T, Int?> {
    return addRule(
        rule("negative", { message }) { it == null || it < 0 }
    )
}

fun <T> FieldValidator<T, Long?>.positive(
    message: String = "Value must be positive"
): FieldValidator<T, Long?> {
    return addRule(
        rule("positive", { message }) { it == null || it > 0 }
    )
}

fun <T> FieldValidator<T, Double?>.positive(
    message: String = "Value must be positive"
): FieldValidator<T, Double?> {
    return addRule(
        rule("positive", { message }) { it == null || it > 0 }
    )
}
