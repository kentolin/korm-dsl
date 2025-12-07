// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/AsyncValidator.kt

package com.korm.dsl.validation

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Async validator interface.
 */
interface AsyncValidator<T> {
    /**
     * Validate asynchronously.
     */
    suspend fun validateAsync(value: T): ValidationResult
}

/**
 * Async entity validator.
 */
class AsyncEntityValidator<T : Any> : AsyncValidator<T> {
    private val validators = mutableListOf<suspend (T) -> ValidationResult>()

    /**
     * Add async validator.
     */
    fun addValidator(validator: suspend (T) -> ValidationResult): AsyncEntityValidator<T> {
        validators.add(validator)
        return this
    }

    override suspend fun validateAsync(value: T): ValidationResult = coroutineScope {
        val results = validators.map { validator ->
            async { validator(value) }
        }.awaitAll()

        val allViolations = results.flatMap { it.violations }

        if (allViolations.isEmpty()) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(allViolations)
        }
    }
}

/**
 * Database uniqueness validator.
 */
class UniquenessValidator<T, V>(
    private val field: String,
    private val valueExtractor: (T) -> V,
    private val checkUnique: suspend (V) -> Boolean,
    private val message: String = "Value must be unique"
) : AsyncValidator<T> {

    override suspend fun validateAsync(value: T): ValidationResult {
        val fieldValue = valueExtractor(value)
        val isUnique = checkUnique(fieldValue)

        return if (isUnique) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(
                ValidationViolation(
                    field = field,
                    message = message,
                    invalidValue = fieldValue
                )
            )
        }
    }
}
