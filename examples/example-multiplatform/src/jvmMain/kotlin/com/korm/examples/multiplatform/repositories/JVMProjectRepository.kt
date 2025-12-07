// examples/example-multiplatform/src/jvmMain/kotlin/com/korm/examples/multiplatform/repositories/JVMProjectRepository.kt

package com.korm.examples.multiplatform.repositories

import com.korm.examples.multiplatform.services.DatabaseProvider

/**
 * JVM-specific Project Repository factory.
 */
object JVMProjectRepository {

    fun create(databasePath: String? = null): ProjectRepository {
        val database = DatabaseProvider(databasePath).provideDatabase()
        return ProjectRepository(database)
    }
}
