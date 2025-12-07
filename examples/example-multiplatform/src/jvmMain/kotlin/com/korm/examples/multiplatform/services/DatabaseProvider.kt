// examples/example-multiplatform/src/jvmMain/kotlin/com/korm/examples/multiplatform/services/DatabaseProvider.kt

package com.korm.examples.multiplatform.services

import com.korm.dsl.core.Database
import java.io.File

/**
 * JVM-specific database provider.
 *
 * Uses SQLite on JVM with user home directory.
 */
actual class DatabaseProvider(private val databasePath: String? = null) {

    actual fun provideDatabase(): Database {
        val dbPath = databasePath ?: getDefaultDatabasePath()

        // Ensure directory exists
        File(dbPath).parentFile?.mkdirs()

        val database = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC"
        )

        // Initialize database
        DatabaseInitializer.initialize(database)

        return database
    }

    private fun getDefaultDatabasePath(): String {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".korm-multiplatform")
        appDir.mkdirs()
        return File(appDir, "korm_multiplatform.db").absolutePath
    }
}
