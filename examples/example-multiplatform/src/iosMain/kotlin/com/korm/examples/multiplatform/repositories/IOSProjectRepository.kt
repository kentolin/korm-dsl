// examples/example-multiplatform/src/iosMain/kotlin/com/korm/examples/multiplatform/repositories/IOSProjectRepository.kt

package com.korm.examples.multiplatform.repositories

import com.korm.examples.multiplatform.services.DatabaseProvider

/**
 * iOS-specific Project Repository factory.
 */
object IOSProjectRepository {

    fun create(): ProjectRepository {
        val database = DatabaseProvider().provideDatabase()
        return ProjectRepository(database)
    }
}
