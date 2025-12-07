// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/TransactionIsolation.kt

package com.korm.dsl.core

import java.sql.Connection

/**
 * Transaction isolation levels.
 */
enum class TransactionIsolation(val level: Int) {
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    companion object {
        fun fromLevel(level: Int): TransactionIsolation? {
            return values().find { it.level == level }
        }
    }
}

/**
 * Transaction configuration.
 */
data class TransactionConfig(
    val isolationLevel: TransactionIsolation = TransactionIsolation.READ_COMMITTED,
    val readOnly: Boolean = false,
    val timeout: Int? = null
)

/**
 * Execute transaction with specific configuration.
 */
fun <T> Database.transaction(
    config: TransactionConfig,
    block: Transaction.() -> T
): T {
    return getConnection().use { connection ->
        val originalIsolation = connection.transactionIsolation
        val originalReadOnly = connection.isReadOnly

        try {
            connection.transactionIsolation = config.isolationLevel.level
            connection.isReadOnly = config.readOnly

            val transaction = Transaction(connection)
            val result = transaction.block()
            connection.commit()
            result
        } catch (e: Exception) {
            connection.rollback()
            throw TransactionException("Transaction failed", e)
        } finally {
            connection.transactionIsolation = originalIsolation
            connection.isReadOnly = originalReadOnly
        }
    }
}
