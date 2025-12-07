// examples/example-multiplatform/src/androidMain/kotlin/com/korm/examples/multiplatform/services/DatabaseProvider.kt

package com.korm.examples.multiplatform.services

import android.content.Context
import com.korm.dsl.core.Database

/**
 * Android-specific database provider.
 *
 * Uses SQLite on Android with app-specific database path.
 */
actual class DatabaseProvider(private val context: Context) {

    actual fun provideDatabase(): Database {
        val dbPath = context.getDatabasePath("korm_multiplatform.db").absolutePath

        val database = Database.connect(
            url = "jdbc:sqlite:$dbPath",
            driver = "org.sqlite.JDBC"
        )

        // Initialize database
        DatabaseInitializer.initialize(database)

        return database
    }
}
