// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/validators/StringValidators.kt

package com.korm.dsl.validation.validators

import com.korm.dsl.validation.FieldValidator
import com.korm.dsl.validation.rule

/**
 * String-specific validation rules.
 */

// Email validator
fun <T> FieldValidator<T, String?>.email(
    message: String = "Invalid email format"
): FieldValidator<T, String?> {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
    return addRule(
        rule("email", { message }) { it == null || emailRegex.matches(it) }
    )
}

// Pattern validator
fun <T> FieldValidator<T, String?>.pattern(
    regex: Regex,
    message: String = "Value does not match required pattern"
): FieldValidator<T, String?> {
    return addRule(
        rule("pattern", { message }) { it == null || regex.matches(it) }
    )
}

fun <T> FieldValidator<T, String?>.pattern(
    pattern: String,
    message: String = "Value does not match required pattern"
): FieldValidator<T, String?> {
    return pattern(pattern.toRegex(), message)
}

// URL validator
fun <T> FieldValidator<T, String?>.url(
    message: String = "Invalid URL format"
): FieldValidator<T, String?> {
    val urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*\$".toRegex()
    return addRule(
        rule("url", { message }) { it == null || urlRegex.matches(it) }
    )
}

// Alpha validator (only letters)
fun <T> FieldValidator<T, String?>.alpha(
    message: String = "Value must contain only letters"
): FieldValidator<T, String?> {
    val alphaRegex = "^[a-zA-Z]+\$".toRegex()
    return addRule(
        rule("alpha", { message }) { it == null || alphaRegex.matches(it) }
    )
}

// Alphanumeric validator
fun <T> FieldValidator<T, String?>.alphanumeric(
    message: String = "Value must contain only letters and numbers"
): FieldValidator<T, String?> {
    val alphanumericRegex = "^[a-zA-Z0-9]+\$".toRegex()
    return addRule(
        rule("alphanumeric", { message }) { it == null || alphanumericRegex.matches(it) }
    )
}

// Numeric validator
fun <T> FieldValidator<T, String?>.numeric(
    message: String = "Value must contain only numbers"
): FieldValidator<T, String?> {
    val numericRegex = "^[0-9]+\$".toRegex()
    return addRule(
        rule("numeric", { message }) { it == null || numericRegex.matches(it) }
    )
}

// Length validators
fun <T> FieldValidator<T, String?>.minLength(
    length: Int,
    message: String = "Value must be at least $length characters"
): FieldValidator<T, String?> {
    return addRule(
        rule("minLength", { message }) { it == null || it.length >= length }
    )
}

fun <T> FieldValidator<T, String?>.maxLength(
    length: Int,
    message: String = "Value must be at most $length characters"
): FieldValidator<T, String?> {
    return addRule(
        rule("maxLength", { message }) { it == null || it.length <= length }
    )
}

// Contains validator
fun <T> FieldValidator<T, String?>.contains(
    substring: String,
    ignoreCase: Boolean = false,
    message: String = "Value must contain '$substring'"
): FieldValidator<T, String?> {
    return addRule(
        rule("contains", { message }) {
            it == null || it.contains(substring, ignoreCase)
        }
    )
}

// Starts/Ends with validators
fun <T> FieldValidator<T, String?>.startsWith(
    prefix: String,
    ignoreCase: Boolean = false,
    message: String = "Value must start with '$prefix'"
): FieldValidator<T, String?> {
    return addRule(
        rule("startsWith", { message }) {
            it == null || it.startsWith(prefix, ignoreCase)
        }
    )
}

fun <T> FieldValidator<T, String?>.endsWith(
    suffix: String,
    ignoreCase: Boolean = false,
    message: String = "Value must end with '$suffix'"
): FieldValidator<T, String?> {
    return addRule(
        rule("endsWith", { message }) {
            it == null || it.endsWith(suffix, ignoreCase)
        }
    )
}

// Phone number validator
fun <T> FieldValidator<T, String?>.phone(
    message: String = "Invalid phone number format"
): FieldValidator<T, String?> {
    // Simple phone validation - accepts various formats
    val phoneRegex = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}\$".toRegex()
    return addRule(
        rule("phone", { message }) { it == null || phoneRegex.matches(it) }
    )
}

// Credit card validator
fun <T> FieldValidator<T, String?>.creditCard(
    message: String = "Invalid credit card number"
): FieldValidator<T, String?> {
    return addRule(
        rule("creditCard", { message }) { value ->
            if (value == null) return@rule true

            // Remove spaces and dashes
            val digits = value.replace(Regex("[\\s-]"), "")

            // Check if all characters are digits
            if (!digits.matches(Regex("^[0-9]+\$"))) return@rule false

            // Luhn algorithm
            var sum = 0
            var alternate = false

            for (i in digits.length - 1 downTo 0) {
                var digit = digits[i].toString().toInt()
                if (alternate) {
                    digit *= 2
                    if (digit > 9) {
                        digit -= 9
                    }
                }
                sum += digit
                alternate = !alternate
            }

            sum % 10 == 0
        }
    )
}

// UUID validator
fun <T> FieldValidator<T, String?>.uuid(
    message: String = "Invalid UUID format"
): FieldValidator<T, String?> {
    val uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$".toRegex()
    return addRule(
        rule("uuid", { message }) { it == null || uuidRegex.matches(it) }
    )
}

// OneOf validator (enum-like)
fun <T> FieldValidator<T, String?>.oneOf(
    vararg values: String,
    ignoreCase: Boolean = false,
    message: String = "Value must be one of: ${values.joinToString(", ")}"
): FieldValidator<T, String?> {
    return addRule(
        rule("oneOf", { message }) { value ->
            if (value == null) return@rule true
            values.any { it.equals(value, ignoreCase) }
        }
    )
}
