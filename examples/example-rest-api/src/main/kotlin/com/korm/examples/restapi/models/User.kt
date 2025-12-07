// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/models/User.kt

package com.korm.examples.restapi.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val active: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 50).unique().notNull()
    val email = varchar("email", 255).unique().notNull()
    val firstName = varchar("first_name", 100).notNull()
    val lastName = varchar("last_name", 100).notNull()
    val active = boolean("active").default(true).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)
}

// DTOs
data class CreateUserRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val active: Boolean? = null
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                active = user.active,
                createdAt = user.createdAt.toString(),
                updatedAt = user.updatedAt.toString()
            )
        }
    }
}
