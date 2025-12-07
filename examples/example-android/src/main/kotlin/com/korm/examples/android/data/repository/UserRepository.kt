// examples/example-android/src/main/kotlin/com/korm/examples/android/data/repository/UserRepository.kt

package com.korm.examples.android.data.repository

import com.korm.examples.android.data.local.dao.UserDao
import com.korm.examples.android.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository providing a clean API for data access.
 * Abstracts the data sources and provides a single source of truth.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Create a new user.
     *
     * Business logic: Validate email uniqueness before insertion
     */
    suspend fun createUser(name: String, email: String, age: Int): Result<UserEntity> {
        return try {
            // Check if email already exists
            if (userDao.emailExists(email)) {
                Result.failure(Exception("Email already exists"))
            } else {
                val user = UserEntity(
                    name = name,
                    email = email,
                    age = age
                )

                val userId = userDao.insert(user)
                val createdUser = userDao.findById(userId)

                if (createdUser != null) {
                    Result.success(createdUser)
                } else {
                    Result.failure(Exception("Failed to create user"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user by ID.
     */
    suspend fun getUserById(id: Long): UserEntity? {
        return userDao.findById(id)
    }

    /**
     * Get all users as Flow for reactive updates.
     */
    fun getAllUsers(): Flow<List<UserEntity>> = flow {
        emit(userDao.findAll())
    }

    /**
     * Get active users.
     */
    suspend fun getActiveUsers(): List<UserEntity> {
        return userDao.findByActiveStatus(true)
    }

    /**
     * Get inactive users.
     */
    suspend fun getInactiveUsers(): List<UserEntity> {
        return userDao.findByActiveStatus(false)
    }

    /**
     * Search users by name.
     */
    fun searchUsers(query: String): Flow<List<UserEntity>> = flow {
        emit(userDao.searchByName(query))
    }

    /**
     * Get users by age range.
     */
    suspend fun getUsersByAgeRange(minAge: Int, maxAge: Int): List<UserEntity> {
        return userDao.findByAgeRange(minAge, maxAge)
    }

    /**
     * Get paginated users.
     */
    suspend fun getUsersPaginated(page: Int, pageSize: Int): List<UserEntity> {
        val offset = (page - 1) * pageSize
        return userDao.findPaginated(pageSize, offset)
    }

    /**
     * Update user.
     */
    suspend fun updateUser(user: UserEntity): Result<UserEntity> {
        return try {
            val updated = userDao.update(user)
            if (updated > 0) {
                val updatedUser = userDao.findById(user.id)
                if (updatedUser != null) {
                    Result.success(updatedUser)
                } else {
                    Result.failure(Exception("Failed to fetch updated user"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggle user's active status.
     */
    suspend fun toggleUserStatus(id: Long): Result<UserEntity> {
        return try {
            val user = userDao.findById(id)
                ?: return Result.failure(Exception("User not found"))

            userDao.updateActiveStatus(id, !user.isActive)
            val updatedUser = userDao.findById(id)!!
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user.
     */
    suspend fun deleteUser(id: Long): Result<Unit> {
        return try {
            val deleted = userDao.delete(id)
            if (deleted > 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all inactive users.
     */
    suspend fun deleteInactiveUsers(): Result<Int> {
        return try {
            val deleted = userDao.deleteInactiveUsers()
            Result.success(deleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user statistics.
     */
    suspend fun getUserStatistics(): UserStatistics {
        val totalUsers = userDao.count()
        val activeUsers = userDao.countByStatus(true)
        val inactiveUsers = userDao.countByStatus(false)
        val averageAge = userDao.getAverageAge()

        return UserStatistics(
            total = totalUsers,
            active = activeUsers,
            inactive = inactiveUsers,
            averageAge = averageAge
        )
    }
}

data class UserStatistics(
    val total: Long,
    val active: Long,
    val inactive: Long,
    val averageAge: Double
)
