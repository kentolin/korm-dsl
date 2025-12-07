// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/models/User.kt

package com.korm.examples.enterprise.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val active: Boolean = true,
    val emailVerified: Boolean = false,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    ADMIN,
    USER,
    MANAGER,
    GUEST
}

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 50).unique().notNull()
    val email = varchar("email", 255).unique().notNull()
    val passwordHash = varchar("password_hash", 255).notNull()
    val firstName = varchar("first_name", 100).notNull()
    val lastName = varchar("last_name", 100).notNull()
    val role = varchar("role", 20).default("USER").notNull()
    val active = boolean("active").default(true).notNull()
    val emailVerified = boolean("email_verified").default(false).notNull()
    val lastLoginAt = timestamp("last_login_at")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_users_email", listOf("email"), unique = true)
        index("idx_users_username", listOf("username"), unique = true)
        index("idx_users_role", listOf("role"))
    }
}
