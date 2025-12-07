// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/repositories/UserRepository.kt

package com.korm.examples.basic.repositories

import com.korm.dsl.core.Database
import com.korm.examples.basic.models.CreateUserDTO
import com.korm.examples.basic.models.UpdateUserDTO
import com.korm.examples.basic.models.User
import com.korm.examples.basic.models.Users
import java.sql.ResultSet

class UserRepository(private val database: Database) {

    /**
     * Create a new user.
     */
    fun create(dto: CreateUserDTO): User {
        val userId = database.transaction {
            val query = insertInto(Users) {
                it[Users.name] = dto.name
                it[Users.email] = dto.email
                dto.age?.let { age -> it[Users.age] = age }
            }
            insert(query)
        }

        return findById(userId) ?: throw IllegalStateException("Failed to create user")
    }

    /**
     * Find user by ID.
     */
    fun findById(id: Long): User? {
        return database.transaction {
            val query = from(Users)
                .where(Users.id eq id)

            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    /**
     * Find all users.
     */
    fun findAll(): List<User> {
        return database.transaction {
            val query = from(Users)
            select(query, ::mapUser)
        }
    }

    /**
     * Find users by name (partial match).
     */
    fun findByName(name: String): List<User> {
        return database.transaction {
            val query = from(Users)
                .where(Users.name like "%$name%")

            select(query, ::mapUser)
        }
    }

    /**
     * Find users by email.
     */
    fun findByEmail(email: String): User? {
        return database.transaction {
            val query = from(Users)
                .where(Users.email eq email)

            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    /**
     * Find active users.
     */
    fun findActive(): List<User> {
        return database.transaction {
            val query = from(Users)
                .where(Users.active eq true)
                .orderBy(Users.createdAt, "DESC")

            select(query, ::mapUser)
        }
    }

    /**
     * Update user.
     */
    fun update(id: Long, dto: UpdateUserDTO): User? {
        database.transaction {
            val query = update(Users) {
                dto.name?.let { name -> it[Users.name] = name }
                dto.email?.let { email -> it[Users.email] = email }
                dto.age?.let { age -> it[Users.age] = age }
                dto.active?.let { active -> it[Users.active] = active }
            }.where(Users.id eq id)

            update(query)
        }

        return findById(id)
    }

    /**
     * Delete user.
     */
    fun delete(id: Long): Boolean {
        val deleted = database.transaction {
            val query = deleteFrom(Users) {
                where(Users.id eq id)
            }
            delete(query)
        }

        return deleted > 0
    }

    /**
     * Count all users.
     */
    fun count(): Long {
        return database.transaction {
            execute("SELECT COUNT(*) FROM users")
            // Simplified - in real implementation, would use proper query builder
            0L
        }
    }

    /**
     * Map ResultSet to User object.
     */
    private fun mapUser(rs: ResultSet): User {
        return User(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            age = rs.getInt("age").takeIf { !rs.wasNull() },
            active = rs.getBoolean("active"),
            createdAt = rs.getLong("created_at")
        )
    }
}
