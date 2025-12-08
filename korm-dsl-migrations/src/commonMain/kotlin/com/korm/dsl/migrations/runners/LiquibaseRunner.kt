// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/runners/LiquibaseRunner.kt

package com.korm.dsl.migrations.runners

import com.korm.dsl.core.Database
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

/**
 * Liquibase migration runner integration.
 */
class LiquibaseRunner(
    private val database: Database,
    private val config: LiquibaseConfig = LiquibaseConfig()
) {

    /**
     * Execute migrations.
     */
    fun migrate() {
        database.getConnection().use { connection ->
            val liquibaseDatabase = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))

            liquibaseDatabase.defaultSchemaName = config.defaultSchema

            Liquibase(
                config.changeLogFile,
                ClassLoaderResourceAccessor(),
                liquibaseDatabase
            ).use { liquibase ->
                liquibase.update(config.contexts)
            }
        }
    }

    /**
     * Rollback migrations.
     */
    fun rollback(count: Int) {
        database.getConnection().use { connection ->
            val liquibaseDatabase = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))

            Liquibase(
                config.changeLogFile,
                ClassLoaderResourceAccessor(),
                liquibaseDatabase
            ).use { liquibase ->
                liquibase.rollback(count, config.contexts)
            }
        }
    }

    /**
     * Rollback to specific tag.
     */
    fun rollbackToTag(tag: String) {
        database.getConnection().use { connection ->
            val liquibaseDatabase = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))

            Liquibase(
                config.changeLogFile,
                ClassLoaderResourceAccessor(),
                liquibaseDatabase
            ).use { liquibase ->
                liquibase.rollback(tag, config.contexts)
            }
        }
    }

    /**
     * Validate changelog.
     */
    fun validate() {
        database.getConnection().use { connection ->
            val liquibaseDatabase = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))

            Liquibase(
                config.changeLogFile,
                ClassLoaderResourceAccessor(),
                liquibaseDatabase
            ).use { liquibase ->
                liquibase.validate()
            }
        }
    }

    /**
     * Generate SQL without executing.
     */
    fun generateSql(): String {
        database.getConnection().use { connection ->
            val liquibaseDatabase = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))

            Liquibase(
                config.changeLogFile,
                ClassLoaderResourceAccessor(),
                liquibaseDatabase
            ).use { liquibase ->
                val writer = java.io.StringWriter()
                liquibase.update(config.contexts, writer)
                return writer.toString()
            }
        }
    }
}

/**
 * Liquibase configuration.
 */
data class LiquibaseConfig(
    val changeLogFile: String = "db/changelog/db.changelog-master.xml",
    val contexts: String? = null,
    val defaultSchema: String? = null
)
