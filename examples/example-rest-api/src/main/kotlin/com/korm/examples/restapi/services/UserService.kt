// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/services/UserService.kt

package com.korm.examples.restapi.services

import com.korm.dsl.core.Database
import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.NotFoundException
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UserService(private val database: Database) {

    fun createUser(request: CreateUserRequest): User {
        val userId = database.transaction {
            val query = insertInto(Users) {
                it[Users.username] = request.username
                it[Users.email] = request.email
                it[Users.firstName] = request.firstName
                it[Users.lastName] = request.lastName
            }
            insert(query)
        }

        return findById(userId) ?: throw IllegalStateException("Failed to create user")
    }

    fun findById(id: Long): User? {
        return database.transaction {
            val query = from(Users).where(Users.id eq id)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    fun findAll(limit: Int = 100, offset: Int = 0): List<User> {
        return database.transaction {
            val query = from(Users)
                .orderBy(Users.createdAt, "DESC")
                .limit(limit)
                .offset(offset)
            select(query, ::mapUser)
        }
    }

    fun findByUsername(username: String): User? {
        return database.transaction {
            val query = from(Users).where(Users.username eq username)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    fun update(id: Long, request: UpdateUserRequest): User {
        database.transaction {
            val query = update(Users) {
                request.email?.let { email -> it[Users.email] = email }
                request.firstName?.let { firstName -> it[Users.firstName] = firstName }
                request.lastName?.let { lastName -> it[Users.lastName] = lastName }
                request.active?.let { active -> it[Users.active] = active }
                it[Users.updatedAt] = System.currentTimeMillis()
            }.where(Users.id eq id)

            update(query)
        }

        return findById(id) ?: throw NotFoundException("User not found")
    }

    fun delete(id: Long): Boolean {
        val deleted = database.transaction {
            val query = deleteFrom(Users) {
                where(Users.id eq id)
            }
            delete(query)
        }

        return deleted > 0
    }

    fun count(): Long {
        return database.transaction {
            val sql = "SELECT COUNT(*) FROM users"
            var count = 0L
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            count = rs.getLong(1)
                        }
                    }
                }
            }
            count
        }
    }

    private fun mapUser(rs: ResultSet): User {
        return User(
            id = rs.getLong("id"),
            username = rs.getString("username"),
            email = rs.getString("email"),
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name"),
            active = rs.getBoolean("active"),
            createdAt = toLocalDateTime(rs.getLong("created_at")),
            updatedAt = toLocalDateTime(rs.getLong("updated_at"))
        )
    }

    private fun toLocalDateTime(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            ZoneId.systemDefault()
        )
    }
}
