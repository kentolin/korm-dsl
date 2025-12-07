// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/MigrationEngine.kt

package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.utils.KormLogger
import java.security.MessageDigest
import java.time.Instant

/**
 * Migration execution engine.
 */
class MigrationEngine(
    private val database: Database,
    private val config: MigrationConfig = MigrationConfig()
) {
    private val historyTable = MigrationHistoryTable(config.tableName)

    init {
        initializeHistoryTable()
    }

    /**
     * Initialize migration history table.
     */
    private fun initializeHistoryTable() {
        database.transaction {
            execute(historyTable.createTableSql())
        }
    }

    /**
     * Execute pending migrations.
     */
    fun migrate(migrations: List<Migration>): List<MigrationResult> {
        val results = mutableListOf<MigrationResult>()
        val appliedVersions = getAppliedVersions()
        val sortedMigrations = migrations.sortedBy { it.version }

        // Validate migration versions
        validateMigrations(sortedMigrations)

        for (migration in sortedMigrations) {
            if (migration.version in appliedVersions) {
                if (config.validateChecksums) {
                    validateChecksum(migration)
                }
                results.add(
                    MigrationResult.Skipped(
                        migration.version,
                        migration.description,
                        "Already applied"
                    )
                )
                continue
            }

            val result = executeMigration(migration)
            results.add(result)

            if (result is MigrationResult.Failure && !config.continueOnError) {
                break
            }
        }

        return results
    }

    /**
     * Execute a single migration.
     */
    private fun executeMigration(migration: Migration): MigrationResult {
        val startTime = System.currentTimeMillis()

        return try {
            KormLogger.info("Executing migration ${migration.version}: ${migration.description}")

            migration.up(database)

            val executionTime = System.currentTimeMillis() - startTime
            val checksum = calculateChecksum(migration)

            // Record in history
            recordMigration(migration, executionTime, true, null, checksum)

            KormLogger.info("Migration ${migration.version} completed in ${executionTime}ms")

            MigrationResult.Success(
                migration.version,
                migration.description,
                executionTime
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            val checksum = calculateChecksum(migration)

            KormLogger.error("Migration ${migration.version} failed", e)

            // Record failure in history
            recordMigration(migration, executionTime, false, e.message, checksum)

            MigrationResult.Failure(
                migration.version,
                migration.description,
                e
            )
        }
    }

    /**
     * Rollback migrations to a specific version.
     */
    fun rollback(migrations: List<Migration>, targetVersion: Long): List<MigrationResult> {
        val results = mutableListOf<MigrationResult>()
        val appliedVersions = getAppliedVersions().sorted().reversed()

        val migrationsMap = migrations.associateBy { it.version }

        for (version in appliedVersions) {
            if (version <= targetVersion) break

            val migration = migrationsMap[version]
            if (migration == null) {
                KormLogger.warn("Migration $version not found, skipping rollback")
                continue
            }

            val result = rollbackMigration(migration)
            results.add(result)

            if (result is MigrationResult.Failure && !config.continueOnError) {
                break
            }
        }

        return results
    }

    /**
     * Rollback a single migration.
     */
    private fun rollbackMigration(migration: Migration): MigrationResult {
        val startTime = System.currentTimeMillis()

        return try {
            KormLogger.info("Rolling back migration ${migration.version}: ${migration.description}")

            migration.down(database)

            val executionTime = System.currentTimeMillis() - startTime

            // Remove from history
            removeMigrationHistory(migration.version)

            KormLogger.info("Migration ${migration.version} rolled back in ${executionTime}ms")

            MigrationResult.Success(
                migration.version,
                migration.description,
                executionTime
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime

            KormLogger.error("Rollback of migration ${migration.version} failed", e)

            MigrationResult.Failure(
                migration.version,
                migration.description,
                e
            )
        }
    }

    /**
     * Get information about all migrations.
     */
    fun getMigrationInfo(migrations: List<Migration>): List<MigrationInfo> {
        val history = getMigrationHistory()
        val historyMap = history.associateBy { it.version }

        return migrations.sortedBy { it.version }.map { migration ->
            val historyEntry = historyMap[migration.version]

            val status = when {
                historyEntry == null -> MigrationStatus.PENDING
                !historyEntry.success -> MigrationStatus.FAILED
                else -> MigrationStatus.APPLIED
            }

            MigrationInfo(
                migration = migration,
                status = status,
                appliedAt = historyEntry?.executedAt,
                executionTimeMs = historyEntry?.executionTimeMs
            )
        }
    }

    /**
     * Validate migrations for version conflicts.
     */
    private fun validateMigrations(migrations: List<Migration>) {
        val versions = migrations.map { it.version }
        val duplicates = versions.groupingBy { it }.eachCount().filter { it.value > 1 }

        if (duplicates.isNotEmpty()) {
            throw MigrationException("Duplicate migration versions found: ${duplicates.keys}")
        }

        // Check for gaps in version numbers if configured
        if (config.allowGaps) return

        val sortedVersions = versions.sorted()
        for (i in 1 until sortedVersions.size) {
            if (sortedVersions[i] - sortedVersions[i - 1] > 1) {
                throw MigrationException(
                    "Gap in migration versions: ${sortedVersions[i - 1]} -> ${sortedVersions[i]}"
                )
            }
        }
    }

    /**
     * Validate checksum for a migration.
     */
    private fun validateChecksum(migration: Migration) {
        val currentChecksum = calculateChecksum(migration)
        val storedChecksum = database.transaction {
            val sql = "SELECT checksum FROM ${config.tableName} WHERE version = ${migration.version}"
            var checksum: String? = null

            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            checksum = rs.getString("checksum")
                        }
                    }
                }
            }
            checksum
        }

        if (storedChecksum != null && storedChecksum != currentChecksum) {
            throw MigrationException(
                "Checksum mismatch for migration ${migration.version}. " +
                    "Migration has been modified after being applied."
            )
        }
    }

    /**
     * Calculate checksum for a migration.
     */
    private fun calculateChecksum(migration: Migration): String {
        val content = "${migration.version}:${migration.description}"
        val bytes = MessageDigest.getInstance("MD5").digest(content.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Get applied migration versions.
     */
    private fun getAppliedVersions(): Set<Long> {
        return database.transaction {
            val sql = "SELECT version FROM ${config.tableName} WHERE success = true"
            val versions = mutableSetOf<Long>()

            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            versions.add(rs.getLong("version"))
                        }
                    }
                }
            }
            versions
        }
    }

    /**
     * Get migration history.
     */
    private fun getMigrationHistory(): List<MigrationHistory> {
        return database.transaction {
            val sql = "SELECT * FROM ${config.tableName} ORDER BY version"
            val history = mutableListOf<MigrationHistory>()

            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            history.add(
                                MigrationHistory(
                                    id = rs.getLong("id"),
                                    version = rs.getLong("version"),
                                    description = rs.getString("description"),
                                    executedAt = Instant.ofEpochMilli(rs.getLong("executed_at")),
                                    executionTimeMs = rs.getLong("execution_time_ms"),
                                    success = rs.getBoolean("success"),
                                    errorMessage = rs.getString("error_message"),
                                    checksum = rs.getString("checksum")
                                )
                            )
                        }
                    }
                }
            }
            history
        }
    }

    /**
     * Record migration in history.
     */
    private fun recordMigration(
        migration: Migration,
        executionTimeMs: Long,
        success: Boolean,
        errorMessage: String?,
        checksum: String
    ) {
        database.transaction {
            val sql = """
                INSERT INTO ${config.tableName}
                (version, description, executed_at, execution_time_ms, success, error_message, checksum)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, migration.version)
                    stmt.setString(2, migration.description)
                    stmt.setLong(3, System.currentTimeMillis())
                    stmt.setLong(4, executionTimeMs)
                    stmt.setBoolean(5, success)
                    stmt.setString(6, errorMessage)
                    stmt.setString(7, checksum)
                    stmt.executeUpdate()
                }
            }
        }
    }

    /**
     * Remove migration from history.
     */
    private fun removeMigrationHistory(version: Long) {
        database.transaction {
            val sql = "DELETE FROM ${config.tableName} WHERE version = ?"

            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, version)
                    stmt.executeUpdate()
                }
            }
        }
    }
}

/**
 * Migration configuration.
 */
data class MigrationConfig(
    val tableName: String = "schema_migrations",
    val validateChecksums: Boolean = true,
    val allowGaps: Boolean = false,
    val continueOnError: Boolean = false
)

/**
 * Migration exception.
 */
class MigrationException(message: String, cause: Throwable? = null) : Exception(message, cause)
