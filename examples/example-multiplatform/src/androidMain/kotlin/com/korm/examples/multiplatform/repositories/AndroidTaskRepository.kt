// examples/example-multiplatform/src/androidMain/kotlin/com/korm/examples/multiplatform/repositories/AndroidTaskRepository.kt

package com.korm.examples.multiplatform.repositories

import android.content.Context
import com.korm.examples.multiplatform.services.DatabaseProvider

/**
 * Android-specific Task Repository factory.
 */
object AndroidTaskRepository {

    fun create(context: Context): TaskRepository {
        val database = DatabaseProvider(context).provideDatabase()
        return TaskRepository(database)
    }
}
