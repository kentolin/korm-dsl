// examples/example-multiplatform/src/iosMain/kotlin/com/korm/examples/multiplatform/services/DatabaseProvider.kt

package com.korm.examples.multiplatform.services

import com.korm.dsl.core.Database
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS-specific database provider.
 *
 * Uses SQLite on iOS with Documents directory.
 */
actual class DatabaseProvider {

    actual fun provideDatabase(): Database {
        val dbPath = getDatabasePath()

        val database = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC"
        )

        // Initialize database
        DatabaseInitializer.initialize(database)

        return database
    }

    private fun getDatabasePath(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )

        return "${documentDirectory?.path}/korm_multiplatform.db"
    }
}
