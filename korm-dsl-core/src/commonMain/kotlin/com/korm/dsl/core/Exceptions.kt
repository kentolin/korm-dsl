// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/Exceptions.kt

package com.korm.dsl.core

/**
 * Base exception for KORM.
 */
open class KormException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when a database connection fails.
 */
class ConnectionException(message: String, cause: Throwable? = null) : KormException(message, cause)

/**
 * Exception thrown when a query fails.
 */
class QueryException(message: String, val sql: String, cause: Throwable? = null) : KormException(message, cause)

/**
 * Exception thrown when a transaction fails.
 */
class TransactionException(message: String, cause: Throwable? = null) : KormException(message, cause)

/**
 * Exception thrown when a constraint violation occurs.
 */
class ConstraintViolationException(
    message: String,
    val constraintName: String? = null,
    cause: Throwable? = null
) : KormException(message, cause)

/**
 * Exception thrown when an entity is not found.
 */
class EntityNotFoundException(message: String, val entityType: String? = null) : KormException(message)

/**
 * Exception thrown when mapping fails.
 */
class MappingException(message: String, cause: Throwable? = null) : KormException(message, cause)

/**
 * Exception thrown when schema creation/modification fails.
 */
class SchemaException(message: String, cause: Throwable? = null) : KormException(message, cause)

/**
 * Exception thrown when a duplicate key is found.
 */
class DuplicateKeyException(message: String, cause: Throwable? = null) : KormException(message, cause)

/**
 * Exception thrown when optimistic locking fails.
 */
class OptimisticLockException(message: String) : KormException(message)
