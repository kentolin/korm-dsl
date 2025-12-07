// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/runners/FlywayRunner.kt

package com.korm.dsl.migrations.runners

import com.korm.dsl.core.Database
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfo
import org.flywaydb.core.api.output.MigrateResult
import javax.sql.DataSource

/**
 * Flyway migration runner integration.
 */
class FlywayRunner(
    private val database: Database,
    private val config: FlywayConfig = FlywayConfig()
) {

    private val flyway: Flyway by lazy {
        Flyway.configure()
            .dataSource(getDataSource())
            .locations(*config.locations.toTypedArray())
            .table(config.table)
            .baselineOnMigrate(config.baselineOnMigrate)
            .baselineVersion(config.baselineVersion)
            .validateOnMigrate(config.validateOnMigrate)
            .cleanDisabled(config.cleanDisabled)
            .outOfOrder(config.outOfOrder)
            .load()
    }

    /**
     * Get DataSource from Database.
     */
    private fun getDataSource(): DataSource {
        // Access the underlying DataSource from Database
        return database.getConnection().metaData.connection as DataSource
    }

    /**
     * Execute migrations.
     */
    fun migrate(): MigrateResult {
        return flyway.migrate()
    }

    /**
     * Get migration info.
     */
    fun info(): Array<MigrationInfo> {
        return flyway.info().all()
    }

    /**
     * Validate migrations.
     */
    fun validate() {
        flyway.validate()
    }

    /**
     * Clean database (WARNING: deletes all data).
     */
    fun clean() {
        flyway.clean()
    }

    /**
     * Baseline the database.
     */
    fun baseline() {
        flyway.baseline()
    }

    /**
     * Repair migration history.
     */
    fun repair() {
        flyway.repair()
    }
}

/**
 * Flyway configuration.
 */
data class FlywayConfig(
    val locations: List<String> = listOf("classpath:db/migration"),
    val table: String = "flyway_schema_history",
    val baselineOnMigrate: Boolean = false,
    val baselineVersion: String = "1",
    val validateOnMigrate: Boolean = true,
    val cleanDisabled: Boolean = true,
    val outOfOrder: Boolean = false
)
