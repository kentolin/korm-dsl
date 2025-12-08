// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/Migration.kt

package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import java.time.Instant

/**
 * Base interface for database migrations.
 */
interface Migration {
    /**
     * Unique version identifier for this migration.
     */
    val version: Long

    /**
     * Description of what this migration does.
     */
    val description: String

    /**
     * Execute the migration (upgrade).
     */
    fun up(database: Database)

    /**
     * Rollback the migration (downgrade).
     */
    fun down(database: Database)
}

/**
 * Abstract base class for migrations.
 */
abstract class AbstractMigration : Migration {
    protected val statements = mutableListOf<String>()
    protected val rollbackStatements = mutableListOf<String>()

    override fun up(database: Database) {
        database.transaction {
            statements.forEach { sql ->
                execute(sql)
            }
        }
    }

    override fun down(database: Database) {
        database.transaction {
            rollbackStatements.forEach { sql ->
                execute(sql)
            }
        }
    }

    /**
     * Add a SQL statement to execute.
     */
    protected fun sql(statement: String) {
        statements.add(statement)
    }

    /**
     * Add a rollback SQL statement.
     */
    protected fun rollback(statement: String) {
        rollbackStatements.add(statement)
    }
}


/**
 * Migration execution result.
 */
sealed class MigrationResult {
    data class Success(
        val version: Long,
        val description: String,
        val executionTimeMs: Long
    ) : MigrationResult()

    data class Failure(
        val version: Long,
        val description: String,
        val error: Throwable
    ) : MigrationResult()

    data class Skipped(
        val version: Long,
        val description: String,
        val reason: String
    ) : MigrationResult()
}

/**
 * Migration status.
 */
enum class MigrationStatus {
    PENDING,
    APPLIED,
    FAILED,
    ROLLED_BACK
}

/**
 * Migration info combining migration and its status.
 */
data class MigrationInfo(
    val migration: Migration,
    val status: MigrationStatus,
    val appliedAt: Instant? = null,
    val executionTimeMs: Long? = null
)
