// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/Query.kt

package com.korm.dsl.core

/**
 * Base interface for all queries.
 */
interface Query {
    /**
     * Convert query to SQL string.
     */
    fun toSql(): String

    /**
     * Get query parameters.
     */
    fun getParameters(): List<Any?>
}

/**
 * Query execution result.
 */
sealed class QueryResult<out T> {
    data class Success<T>(val value: T) : QueryResult<T>()
    data class Failure(val error: Throwable) : QueryResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }

    inline fun <R> map(transform: (T) -> R): QueryResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun onSuccess(action: (T) -> Unit): QueryResult<T> {
        if (this is Success) action(value)
        return this
    }

    inline fun onFailure(action: (Throwable) -> Unit): QueryResult<T> {
        if (this is Failure) action(error)
        return this
    }
}

/**
 * Execute a query safely and return a result.
 */
inline fun <T> executeQuery(block: () -> T): QueryResult<T> {
    return try {
        QueryResult.Success(block())
    } catch (e: Exception) {
        QueryResult.Failure(e)
    }
}
