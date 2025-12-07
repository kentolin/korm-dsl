// examples/example-multiplatform/src/commonMain/kotlin/com/korm/examples/multiplatform/services/DatabaseProvider.kt

package com.korm.examples.multiplatform.services

import com.korm.dsl.core.Database

/**
 * Platform-specific database provider.
 * Each platform (Android, iOS, JVM) provides its own implementation.
 */
expect class DatabaseProvider {
    fun provideDatabase(): Database
}
