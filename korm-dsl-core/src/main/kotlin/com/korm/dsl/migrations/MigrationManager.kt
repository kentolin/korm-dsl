package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.core.transaction
import com.korm.dsl.query.*
import com.korm.dsl.schema.create
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Exception thrown when migration operations fail
 */
class MigrationException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Manager for database migrations
 * Handles applying and rolling back schema changes
 */
class MigrationManager(
    private val db: Database,
    private val migrations: List<Migration>
) {

    private val dateFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    init {
        // Validate migrations have unique versions
        val versions = migrations.map { it.version }
        val duplicates = versions.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            throw MigrationException("Duplicate migration versions found: ${duplicates.keys}")
        }

        // Sort migrations by version
        val sortedMigrations = migrations.sortedBy { it.version }
        if (sortedMigrations != migrations) {
            throw MigrationException("Migrations must be provided in version order")
        }
    }

    /**
     * Initialize migration system - creates migration history table
     */
    fun initialize() {
        try {
            MigrationHistory.create(db)
            println("✓ Migration history table initialized")
        } catch (e: Exception) {
            // Table might already exist, that's okay
            println("✓ Migration history table already exists")
        }
    }

    /**
     * Get list of applied migrations from database
     */
    fun getAppliedMigrations(): List<MigrationRecord> {
        return try {
            MigrationHistory.select(db)
                .orderBy(MigrationHistory.version, asc = true)
                .execute { rs ->
                    MigrationRecord(
                        version = rs.getLong("version"),
                        description = rs.getString("description"),
                        appliedAt = rs.getString("applied_at"),
                        executionTimeMs = rs.getLong("execution_time_ms")
                    )
                }
        } catch (e: Exception) {
            // Table doesn't exist yet
            emptyList()
        }
    }

    /**
     * Get list of pending migrations (not yet applied)
     */
    fun getPendingMigrations(): List<Migration> {
        val appliedVersions = getAppliedMigrations().map { it.version }.toSet()
        return migrations.filter { it.version !in appliedVersions }
    }

    /**
     * Migrate to latest version (apply all pending migrations)
     */
    fun migrate(): MigrationResult {
        initialize()

        val pending = getPendingMigrations()
        if (pending.isEmpty()) {
            println("✓ Database is up to date")
            return MigrationResult(applied = 0, failed = 0)
        }

        println("Found ${pending.size} pending migration(s)")

        var applied = 0
        var failed = 0

        for (migration in pending) {
            try {
                applyMigration(migration)
                applied++
            } catch (e: Exception) {
                failed++
                println("✗ Migration ${migration.version} failed: ${e.message}")
                throw MigrationException(
                    "Migration ${migration.version} failed: ${e.message}",
                    e
                )
            }
        }

        println("✓ Applied $applied migration(s)")
        return MigrationResult(applied, failed)
    }

    /**
     * Migrate to a specific version
     */
    fun migrateTo(targetVersion: Long): MigrationResult {
        initialize()

        val appliedVersions = getAppliedMigrations().map { it.version }.toSet()
        val currentVersion = appliedVersions.maxOrNull() ?: 0

        return when {
            targetVersion > currentVersion -> migrateUp(targetVersion)
            targetVersion < currentVersion -> migrateDown(targetVersion)
            else -> {
                println("✓ Already at version $targetVersion")
                MigrationResult(applied = 0, failed = 0)
            }
        }
    }

    /**
     * Rollback the last migration
     */
    fun rollback(steps: Int = 1): MigrationResult {
        initialize()

        val applied = getAppliedMigrations()
        if (applied.isEmpty()) {
            println("✓ No migrations to rollback")
            return MigrationResult(applied = 0, failed = 0)
        }

        val toRollback = applied.takeLast(steps.coerceAtMost(applied.size))
        println("Rolling back ${toRollback.size} migration(s)")

        var rolledBack = 0
        var failed = 0

        for (record in toRollback.reversed()) {
            val migration = migrations.find { it.version == record.version }
            if (migration == null) {
                println("✗ Migration ${record.version} not found in migration list")
                failed++
                continue
            }

            try {
                rollbackMigration(migration)
                rolledBack++
            } catch (e: Exception) {
                failed++
                println("✗ Rollback of migration ${migration.version} failed: ${e.message}")
                throw MigrationException(
                    "Rollback of migration ${migration.version} failed: ${e.message}",
                    e
                )
            }
        }

        println("✓ Rolled back $rolledBack migration(s)")
        return MigrationResult(applied = rolledBack, failed = failed)
    }

    /**
     * Get current migration status
     */
    fun status(): MigrationStatus {
        val applied = getAppliedMigrations()
        val pending = getPendingMigrations()
        val currentVersion = applied.maxOfOrNull { it.version } ?: 0

        return MigrationStatus(
            currentVersion = currentVersion,
            appliedMigrations = applied,
            pendingMigrations = pending,
            totalMigrations = migrations.size
        )
    }

    /**
     * Print migration status
     */
    fun printStatus() {
        val status = status()

        println("\n=== Migration Status ===")
        println("Current version: ${status.currentVersion}")
        println("Applied migrations: ${status.appliedMigrations.size}")
        println("Pending migrations: ${status.pendingMigrations.size}")
        println("Total migrations: ${status.totalMigrations}")

        if (status.appliedMigrations.isNotEmpty()) {
            println("\n--- Applied Migrations ---")
            status.appliedMigrations.forEach { migration ->
                println("  ✓ ${migration.version} - ${migration.description} (${migration.appliedAt})")
            }
        }

        if (status.pendingMigrations.isNotEmpty()) {
            println("\n--- Pending Migrations ---")
            status.pendingMigrations.forEach { migration ->
                println("  ○ ${migration.version} - ${migration.description}")
            }
        }

        println("========================\n")
    }

    // Private helper methods

    private fun migrateUp(targetVersion: Long): MigrationResult {
        val pending = getPendingMigrations()
            .filter { it.version <= targetVersion }

        var applied = 0
        var failed = 0

        for (migration in pending) {
            try {
                applyMigration(migration)
                applied++
            } catch (e: Exception) {
                failed++
                throw MigrationException(
                    "Migration ${migration.version} failed: ${e.message}",
                    e
                )
            }
        }

        return MigrationResult(applied, failed)
    }

    private fun migrateDown(targetVersion: Long): MigrationResult {
        val toRollback = getAppliedMigrations()
            .filter { it.version > targetVersion }
            .reversed()

        var rolledBack = 0
        var failed = 0

        for (record in toRollback) {
            val migration = migrations.find { it.version == record.version }
            if (migration != null) {
                try {
                    rollbackMigration(migration)
                    rolledBack++
                } catch (e: Exception) {
                    failed++
                    throw MigrationException(
                        "Rollback of migration ${migration.version} failed: ${e.message}",
                        e
                    )
                }
            }
        }

        return MigrationResult(applied = rolledBack, failed = failed)
    }

    private fun applyMigration(migration: Migration) {
        println("→ Applying migration ${migration.version}: ${migration.description}")

        val startTime = System.currentTimeMillis()

        db.transaction { conn ->
            // Execute the migration
            migration.up(db, conn)

            // Record the migration in history
            val executionTime = System.currentTimeMillis() - startTime
            val now = dateFormatter.format(Instant.now())

            MigrationHistory.insert(db)
                .set(MigrationHistory.version, migration.version)
                .set(MigrationHistory.description, migration.description)
                .set(MigrationHistory.appliedAt, now)
                .set(MigrationHistory.executionTimeMs, executionTime)
                .execute()
        }

        val executionTime = System.currentTimeMillis() - startTime
        println("  ✓ Completed in ${executionTime}ms")
    }

    private fun rollbackMigration(migration: Migration) {
        println("← Rolling back migration ${migration.version}: ${migration.description}")

        val startTime = System.currentTimeMillis()

        db.transaction { conn ->
            // Execute the rollback
            migration.down(db, conn)

            // Remove from migration history
            MigrationHistory.delete(db)
                .where(MigrationHistory.version, migration.version)
                .execute()
        }

        val executionTime = System.currentTimeMillis() - startTime
        println("  ✓ Rolled back in ${executionTime}ms")
    }
}

/**
 * Result of migration operation
 */
data class MigrationResult(
    val applied: Int,
    val failed: Int
) {
    val isSuccess: Boolean get() = failed == 0
}

/**
 * Current migration status
 */
data class MigrationStatus(
    val currentVersion: Long,
    val appliedMigrations: List<MigrationRecord>,
    val pendingMigrations: List<Migration>,
    val totalMigrations: Int
)
