package com.korm.dsl.validation

/**
 * Not null validation
 */
class NotNullRule<T>(
    override val fieldName: String,
    override val message: String? = null
) : ValidationRule<T> {
    override fun validate(value: T?): ValidationResult {
        return if (value == null) {
            ValidationResult.Invalid(message ?: "$fieldName is required")
        } else {
            ValidationResult.Valid
        }
    }
}

/**
 * String length validation
 */
class StringLengthRule(
    override val fieldName: String,
    private val min: Int? = null,
    private val max: Int? = null,
    override val message: String? = null
) : ValidationRule<String> {
    override fun validate(value: String?): ValidationResult {
        if (value == null) return ValidationResult.Valid

        val errors = mutableListOf<String>()

        min?.let {
            if (value.length < it) {
                errors.add(message ?: "$fieldName must be at least $it characters")
            }
        }

        max?.let {
            if (value.length > it) {
                errors.add(message ?: "$fieldName must be at most $it characters")
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
 * Numeric range validation
 */
class RangeRule<T : Number>(
    override val fieldName: String,
    private val min: T? = null,
    private val max: T? = null,
    override val message: String? = null
) : ValidationRule<T> {
    override fun validate(value: T?): ValidationResult {
        if (value == null) return ValidationResult.Valid

        val errors = mutableListOf<String>()
        val doubleValue = value.toDouble()

        min?.let {
            if (doubleValue < it.toDouble()) {
                errors.add(message ?: "$fieldName must be at least $it")
            }
        }

        max?.let {
            if (doubleValue > it.toDouble()) {
                errors.add(message ?: "$fieldName must be at most $it")
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
 * Email validation
 */
class EmailRule(
    override val fieldName: String,
    override val message: String? = null
) : ValidationRule<String> {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    override fun validate(value: String?): ValidationResult {
        if (value == null) return ValidationResult.Valid

        return if (emailRegex.matches(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(message ?: "$fieldName must be a valid email address")
        }
    }
}

/**
 * Pattern validation (regex)
 */
class PatternRule(
    override val fieldName: String,
    private val pattern: Regex,
    override val message: String? = null
) : ValidationRule<String> {
    constructor(
        fieldName: String,
        pattern: String,
        message: String? = null
    ) : this(fieldName, pattern.toRegex(), message)

    override fun validate(value: String?): ValidationResult {
        if (value == null) return ValidationResult.Valid

        return if (pattern.matches(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(message ?: "$fieldName must match pattern $pattern")
        }
    }
}

/**
 * Custom validation rule
 */
class CustomRule<T>(
    override val fieldName: String,
    override val message: String? = null,
    private val predicate: (T?) -> Boolean
) : ValidationRule<T> {
    override fun validate(value: T?): ValidationResult {
        return if (predicate(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(message ?: "$fieldName validation failed")
        }
    }
}

/**
 * One of validation (enum-like)
 */
class OneOfRule<T>(
    override val fieldName: String,
    private val allowedValues: Set<T>,
    override val message: String? = null
) : ValidationRule<T> {
    constructor(
        fieldName: String,
        vararg allowedValues: T,
        message: String? = null
    ) : this(fieldName, allowedValues.toSet(), message)

    override fun validate(value: T?): ValidationResult {
        if (value == null) return ValidationResult.Valid

        return if (value in allowedValues) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                message ?: "$fieldName must be one of: ${allowedValues.joinToString(", ")}"
            )
        }
    }
}

// Builder functions for easier rule creation
fun <T> notNull(fieldName: String, message: String? = null) =
    NotNullRule<T>(fieldName, message)

fun stringLength(fieldName: String, min: Int? = null, max: Int? = null, message: String? = null) =
    StringLengthRule(fieldName, min, max, message)

fun <T : Number> range(fieldName: String, min: T? = null, max: T? = null, message: String? = null) =
    RangeRule(fieldName, min, max, message)

fun email(fieldName: String, message: String? = null) =
    EmailRule(fieldName, message)

fun pattern(fieldName: String, pattern: String, message: String? = null) =
    PatternRule(fieldName, pattern, message)

fun <T : Any> custom(fieldName: String, message: String? = null, predicate: (T?) -> Boolean): CustomRule<T> =
    CustomRule<T>(fieldName, message, predicate)

fun <T> customNullable(fieldName: String, message: String? = null, predicate: (T?) -> Boolean): CustomRule<T> =
    CustomRule<T>(fieldName, message, predicate)

fun <T> oneOf(fieldName: String, vararg values: T, message: String? = null) =
    OneOfRule(fieldName, *values, message = message)