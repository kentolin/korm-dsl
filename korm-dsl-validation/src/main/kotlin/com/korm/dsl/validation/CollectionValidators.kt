// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/validators/CollectionValidators.kt

package com.korm.dsl.validation.validators

import com.korm.dsl.validation.FieldValidator
import com.korm.dsl.validation.rule

/**
 * Collection-specific validation rules.
 */

// Not empty validator
fun <T, C : Collection<*>> FieldValidator<T, C?>.notEmpty(
    message: String = "Collection must not be empty"
): FieldValidator<T, C?> {
    return addRule(
        rule("notEmpty", { message }) { it == null || it.isNotEmpty() }
    )
}

// Contains validator
fun <T, E> FieldValidator<T, Collection<E>?>.contains(
    element: E,
    message: String = "Collection must contain $element"
): FieldValidator<T, Collection<E>?> {
    return addRule(
        rule("contains", { message }) { it == null || it.contains(element) }
    )
}

// Contains all validator
fun <T, E> FieldValidator<T, Collection<E>?>.containsAll(
    elements: Collection<E>,
    message: String = "Collection must contain all of: ${elements.joinToString(", ")}"
): FieldValidator<T, Collection<E>?> {
    return addRule(
        rule("containsAll", { message }) { it == null || it.containsAll(elements) }
    )
}

// Each element validator
fun <T, E> FieldValidator<T, Collection<E>?>.each(
    predicate: (E) -> Boolean,
    message: String = "All elements must satisfy the condition"
): FieldValidator<T, Collection<E>?> {
    return addRule(
        rule("each", { message }) { it == null || it.all(predicate) }
    )
}

// Any element validator
fun <T, E> FieldValidator<T, Collection<E>?>.any(
    predicate: (E) -> Boolean,
    message: String = "At least one element must satisfy the condition"
): FieldValidator<T, Collection<E>?> {
    return addRule(
        rule("any", { message }) { it == null || it.any(predicate) }
    )
}

// None element validator
fun <T, E> FieldValidator<T, Collection<E>?>.none(
    predicate: (E) -> Boolean,
    message: String = "No element must satisfy the condition"
): FieldValidator<T, Collection<E>?> {
    return addRule(
        rule("none", { message }) { it == null || it.none(predicate) }
    )
}

// Unique elements validator
fun <T, E> FieldValidator<T, Collection<E>?>.unique(
    message: String = "Collection must contain unique elements"
): FieldValidator<T, Collection<E>?> {
    return addRule(
        rule("unique", { message }) { it == null || it.size == it.toSet().size }
    )
}
