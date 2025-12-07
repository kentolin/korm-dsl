// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/services/UserService.kt

package com.korm.examples.enterprise.services

import com.korm.dsl.cache.Cache
import com.korm.dsl.core.Database
import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.examples.enterprise.models.*
import org.mindrot.jbcrypt.BCrypt
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UserService(
    private val database: Database,
    private val cache: Cache<String, Any>,
    private val metrics: DatabaseMetrics,
    private val queryMonitor: QueryMonitor
) {

    fun createUser(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole = UserRole.USER
    ): User {
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        val startTime = System.nanoTime()

        val userId = database.transaction {
            val query = insertInto(Users) {
                it[Users.username] = username
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.firstName] = firstName
                it[Users.lastName] = lastName
                it[Users.role] = role.name
            }
            insert(query)
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordInsert()
        queryMonitor.recordQuery("INSERT INTO users", emptyMap(), duration)

        return findById(userId)!!
    }

    fun findById(id: Long): User? {
        val cacheKey = "user:$id"

        // Try cache first
        cache.get(cacheKey)?.let {
            return it as User
        }

        val startTime = System.nanoTime()

        val user = database.transaction {
            val query = from(Users).where(Users.id eq id)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordSelect()
        queryMonitor.recordQuery("SELECT FROM users WHERE id = ?", mapOf("id" to id), duration)

        // Cache the result
        user?.let { cache.put(cacheKey, it) }

        return user
    }

    fun findByEmail(email: String): User? {
        val startTime = System.nanoTime()

        val user = database.transaction {
            val query = from(Users).where(Users.email eq email)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordSelect()
        queryMonitor.recordQuery("SELECT FROM users WHERE email = ?", mapOf("email" to email), duration)

        return user
    }

    fun findAll(limit: Int = 100, offset: Int = 0): List<User> {
        val startTime = System.nanoTime()

        val users = database.transaction {
            val query = from(Users)
                .orderBy(Users.createdAt, "DESC")
                .limit(limit)
                .offset(offset)
            select(query, ::mapUser)
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordSelect()
        queryMonitor.recordQuery("SELECT FROM users LIMIT ? OFFSET ?",
            mapOf("limit" to limit, "offset" to offset), duration)

        return users
    }

    fun authenticate(email: String, password: String): User? {
        val user = findByEmail(email) ?: return null

        if (!BCrypt.checkpw(password, user.passwordHash)) {
            return null
        }

        // Update last login
        database.transaction {
            update(Users) {
                it[Users.lastLoginAt] = System.currentTimeMillis()
            }.where(Users.id eq user.id).let { update(it) }
        }

        // Invalidate cache
        cache.remove("user:${user.id}")

        return findById(user.id)
    }

    fun updateUser(id: Long, updates: Map<String, Any>): User {
        val startTime = System.nanoTime()

        database.transaction {
            val query = update(Users) {
                updates.forEach { (key, value) ->
                    when (key) {
                        "email" -> it[Users.email] = value as String
                        "firstName" -> it[Users.firstName] = value as String
                        "lastName" -> it[Users.lastName] = value as String
                        "role" -> it[Users.role] = (value as UserRole).name
                        "active" -> it[Users.active] = value as Boolean
                    }
                }
                it[Users.updatedAt] = System.currentTimeMillis()
            }.where(Users.id eq id)

            update(query)
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordUpdate()
        queryMonitor.recordQuery("UPDATE users WHERE id = ?", mapOf("id" to id), duration)

        // Invalidate cache
        cache.remove("user:$id")

        return findById(id)!!
    }

    fun delete(id: Long): Boolean {
        val startTime = System.nanoTime()

        val deleted = database.transaction {
            val query = deleteFrom(Users) {
                where(Users.id eq id)
            }
            delete(query)
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordDelete()
        queryMonitor.recordQuery("DELETE FROM users WHERE id = ?", mapOf("id" to id), duration)

        // Invalidate cache
        cache.remove("user:$id")

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
            passwordHash = rs.getString("password_hash"),
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name"),
            role = UserRole.valueOf(rs.getString("role")),
            active = rs.getBoolean("active"),
            emailVerified = rs.getBoolean("email_verified"),
            lastLoginAt = rs.getLong("last_login_at").takeIf { it > 0 }?.let {
                toLocalDateTime(it)
            },
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
