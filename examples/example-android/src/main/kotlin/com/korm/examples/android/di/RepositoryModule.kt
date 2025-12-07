// examples/example-android/src/main/kotlin/com/korm/examples/android/di/RepositoryModule.kt

package com.korm.examples.android.di

import android.content.Context
import com.korm.examples.android.data.local.dao.UserDao
import com.korm.examples.android.data.repository.UserRepository

/**
 * Repository module for dependency injection.
 */
object RepositoryModule {

    private var userRepository: UserRepository? = null

    /**
     * Provide UserRepository instance.
     */
    fun provideUserRepository(context: Context): UserRepository {
        return userRepository ?: synchronized(this) {
            userRepository ?: createUserRepository(context).also { userRepository = it }
        }
    }

    private fun createUserRepository(context: Context): UserRepository {
        val database = DatabaseModule.provideDatabase(context)
        val userDao = UserDao(database)
        return UserRepository(userDao)
    }
}
