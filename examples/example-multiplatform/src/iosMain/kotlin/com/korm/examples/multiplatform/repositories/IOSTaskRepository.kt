// examples/example-multiplatform/src/iosMain/kotlin/com/korm/examples/multiplatform/repositories/IOSTaskRepository.kt

package com.korm.examples.multiplatform.repositories

import com.korm.examples.multiplatform.services.DatabaseProvider

/**
 * iOS-specific Task Repository factory.
 */
object IOSTaskRepository {

    fun create(): TaskRepository {
        val database = DatabaseProvider().provideDatabase()
        return TaskRepository(database)
    }
}
