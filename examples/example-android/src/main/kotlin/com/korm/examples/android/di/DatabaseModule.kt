// examples/example-android/src/main/kotlin/com/korm/examples/android/di/DatabaseModule.kt

package com.korm.examples.android.di

import android.content.Context
import com.korm.dsl.core.Database
import com.korm.examples.android.data.local.entities.Users

/**
 * Database module for dependency injection.
 *
 * KORM-DSL Usage: Database initialization and table creation
 */
object DatabaseModule {

    private var database: Database? = null

    /**
     * Provide singleton Database instance.
     */
    fun provideDatabase(context: Context): Database {
        return database ?: synchronized(this) {
            database ?: createDatabase(context).also { database = it }
        }
    }

    /**
     * Create and configure database.
     *
     * KORM-DSL Usage:
     * 1. Connect to SQLite database
     * 2. Create tables using Table definitions
     */
    private fun createDatabase(context: Context): Database {
        val dbPath = context.getDatabasePath("korm_app.db").absolutePath

        // Connect to SQLite database
        val db = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC"
        )

        // Create tables
        db.createTables(Users)

        // Initialize with sample data if needed
        initializeSampleData(db)

        return db
    }

    /**
     * Initialize database with sample data.
     *
     * KORM-DSL Usage: Transaction for multiple inserts
     */
    private fun initializeSampleData(database: Database) {
        database.transaction {
            // Check if data already exists
            var hasData = false
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT COUNT(*) FROM users").use { rs ->
                        if (rs.next() && rs.getInt(1) > 0) {
                            hasData = true
                        }
                    }
                }
            }

            // Insert sample data if empty
            if (!hasData) {
                val sampleUsers = listOf(
                    Triple("John Doe", "john@example.com", 28),
                    Triple("Jane Smith", "jane@example.com", 32),
                    Triple("Bob Wilson", "bob@example.com", 45),
                    Triple("Alice Johnson", "alice@example.com", 25),
                    Triple("Charlie Brown", "charlie@example.com", 38)
                )

                sampleUsers.forEach { (name, email, age) ->
                    insertInto(Users) {
                        it[Users.name] = name
                        it[Users.email] = email
                        it[Users.age] = age
                        it[Users.isActive] = true
                    }.let { insert(it) }
                }
            }
        }
    }

    /**
     * Close database connection.
     */
    fun closeDatabase() {
        database?.close()
        database = null
    }
}
