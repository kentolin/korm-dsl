// examples/example-multiplatform/src/jvmMain/kotlin/com/korm/examples/multiplatform/repositories/JVMTaskRepository.kt

package com.korm.examples.multiplatform.repositories

import com.korm.examples.multiplatform.services.DatabaseProvider

/**
 * JVM-specific Task Repository factory.
 */
object JVMTaskRepository {

    fun create(databasePath: String? = null): TaskRepository {
        val database = DatabaseProvider(databasePath).provideDatabase()
        return TaskRepository(database)
    }
}
