// examples/example-android/src/main/kotlin/com/korm/examples/android/data/local/dao/UserDao.kt

package com.korm.examples.android.data.local.dao

import com.korm.dsl.core.Database
import com.korm.examples.android.data.local.entities.UserEntity
import com.korm.examples.android.data.local.entities.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet

class UserDao(private val database: Database) {

    /**
     * Insert a new user into the database.
     *
     * KORM-DSL Usage: insertInto() with column mapping
     */
    suspend fun insert(user: UserEntity): Long = withContext(Dispatchers.IO) {
        database.transaction {
            val query = insertInto(Users) {
                it[Users.name] = user.name
                it[Users.email] = user.email
                it[Users.age] = user.age
                it[Users.isActive] = user.isActive
            }
            insert(query)
        }
    }

    /**
     * Insert multiple users in a batch.
     *
     * KORM-DSL Usage: Batch inserts within a transaction
     */
    suspend fun insertAll(users: List<UserEntity>): List<Long> = withContext(Dispatchers.IO) {
        database.transaction {
            users.map { user ->
                val query = insertInto(Users) {
                    it[Users.name] = user.name
                    it[Users.email] = user.email
                    it[Users.age] = user.age
                    it[Users.isActive] = user.isActive
                }
                insert(query)
            }
        }
    }

    /**
     * Find user by ID.
     *
     * KORM-DSL Usage: from().where() with eq operator
     */
    suspend fun findById(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users).where(Users.id eq id)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    /**
     * Find all users.
     *
     * KORM-DSL Usage: from() with orderBy
     */
    suspend fun findAll(): List<UserEntity> = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users).orderBy(Users.createdAt, "DESC")
            select(query, ::mapUser)
        }
    }

    /**
     * Find users by active status.
     *
     * KORM-DSL Usage: where() with boolean comparison
     */
    suspend fun findByActiveStatus(isActive: Boolean): List<UserEntity> = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users)
                .where(Users.isActive eq isActive)
                .orderBy(Users.name)
            select(query, ::mapUser)
        }
    }

    /**
     * Find user by email.
     *
     * KORM-DSL Usage: where() with string comparison
     */
    suspend fun findByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users).where(Users.email eq email)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    /**
     * Search users by name pattern.
     *
     * KORM-DSL Usage: where() with LIKE operator
     */
    suspend fun searchByName(name: String): List<UserEntity> = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users)
                .where(Users.name like "%$name%")
                .orderBy(Users.name)
            select(query, ::mapUser)
        }
    }

    /**
     * Find users by age range.
     *
     * KORM-DSL Usage: Complex where clause with AND operator
     */
    suspend fun findByAgeRange(minAge: Int, maxAge: Int): List<UserEntity> = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users)
                .where((Users.age gte minAge) and (Users.age lte maxAge))
                .orderBy(Users.age)
            select(query, ::mapUser)
        }
    }

    /**
     * Find users older than specified age.
     *
     * KORM-DSL Usage: where() with gt operator
     */
    suspend fun findUsersOlderThan(age: Int): List<UserEntity> = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users)
                .where(Users.age gt age)
                .orderBy(Users.age, "DESC")
            select(query, ::mapUser)
        }
    }

    /**
     * Get paginated users.
     *
     * KORM-DSL Usage: limit() and offset() for pagination
     */
    suspend fun findPaginated(limit: Int, offset: Int): List<UserEntity> = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users)
                .orderBy(Users.createdAt, "DESC")
                .limit(limit)
                .offset(offset)
            select(query, ::mapUser)
        }
    }

    /**
     * Update user.
     *
     * KORM-DSL Usage: update() with column updates
     */
    suspend fun update(user: UserEntity): Int = withContext(Dispatchers.IO) {
        database.transaction {
            val query = update(Users) {
                it[Users.name] = user.name
                it[Users.email] = user.email
                it[Users.age] = user.age
                it[Users.isActive] = user.isActive
                it[Users.updatedAt] = System.currentTimeMillis()
            }.where(Users.id eq user.id)

            update(query)
        }
    }

    /**
     * Update user's active status.
     *
     * KORM-DSL Usage: Partial update of specific column
     */
    suspend fun updateActiveStatus(id: Long, isActive: Boolean): Int = withContext(Dispatchers.IO) {
        database.transaction {
            val query = update(Users) {
                it[Users.isActive] = isActive
                it[Users.updatedAt] = System.currentTimeMillis()
            }.where(Users.id eq id)

            update(query)
        }
    }

    /**
     * Delete user by ID.
     *
     * KORM-DSL Usage: deleteFrom() with where clause
     */
    suspend fun delete(id: Long): Int = withContext(Dispatchers.IO) {
        database.transaction {
            val query = deleteFrom(Users) {
                where(Users.id eq id)
            }
            delete(query)
        }
    }

    /**
     * Delete inactive users.
     *
     * KORM-DSL Usage: Conditional delete
     */
    suspend fun deleteInactiveUsers(): Int = withContext(Dispatchers.IO) {
        database.transaction {
            val query = deleteFrom(Users) {
                where(Users.isActive eq false)
            }
            delete(query)
        }
    }

    /**
     * Count total users.
     *
     * KORM-DSL Usage: Raw SQL execution for aggregation
     */
    suspend fun count(): Long = withContext(Dispatchers.IO) {
        database.transaction {
            var count = 0L
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT COUNT(*) FROM users").use { rs ->
                        if (rs.next()) {
                            count = rs.getLong(1)
                        }
                    }
                }
            }
            count
        }
    }

    /**
     * Count users by active status.
     *
     * KORM-DSL Usage: Parameterized query for counting
     */
    suspend fun countByStatus(isActive: Boolean): Long = withContext(Dispatchers.IO) {
        database.transaction {
            var count = 0L
            getConnection().use { conn ->
                conn.prepareStatement("SELECT COUNT(*) FROM users WHERE is_active = ?").use { stmt ->
                    stmt.setBoolean(1, isActive)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            count = rs.getLong(1)
                        }
                    }
                }
            }
            count
        }
    }

    /**
     * Get average age of users.
     *
     * KORM-DSL Usage: Aggregate function
     */
    suspend fun getAverageAge(): Double = withContext(Dispatchers.IO) {
        database.transaction {
            var avg = 0.0
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT AVG(age) FROM users").use { rs ->
                        if (rs.next()) {
                            avg = rs.getDouble(1)
                        }
                    }
                }
            }
            avg
        }
    }

    /**
     * Delete all users.
     *
     * KORM-DSL Usage: Delete without where clause
     */
    suspend fun deleteAll(): Int = withContext(Dispatchers.IO) {
        database.transaction {
            val query = deleteFrom(Users) { }
            delete(query)
        }
    }

    /**
     * Check if email exists.
     *
     * KORM-DSL Usage: Existence check
     */
    suspend fun emailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        database.transaction {
            val query = from(Users).where(Users.email eq email)
            val results = select(query, ::mapUser)
            results.isNotEmpty()
        }
    }

    /**
     * Map ResultSet to UserEntity.
     *
     * KORM-DSL Usage: Custom result mapping
     */
    private fun mapUser(rs: ResultSet): UserEntity {
        return UserEntity(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            age = rs.getInt("age"),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getLong("created_at"),
            updatedAt = rs.getLong("updated_at")
        )
    }
}
