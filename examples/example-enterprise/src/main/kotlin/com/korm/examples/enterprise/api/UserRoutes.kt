// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/UserRoutes.kt

package com.korm.examples.enterprise.api

import com.korm.examples.enterprise.models.UserRole
import com.korm.examples.enterprise.services.AuditService
import com.korm.examples.enterprise.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService, auditService: AuditService) {
    route("/api/users") {
        // Create user
        post {
            val request = call.receive<CreateUserRequest>()

            val user = userService.createUser(
                username = request.username,
                email = request.email,
                password = request.password,
                firstName = request.firstName,
                lastName = request.lastName,
                role = request.role ?: UserRole.USER
            )

            // Audit log
            auditService.logAction(
                userId = null,
                action = "CREATE_USER",
                entityType = "User",
                entityId = user.id,
                newValue = "username=${user.username}, email=${user.email}"
            )

            call.respond(HttpStatusCode.Created, UserResponse.from(user))
        }

        // List users
        get {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

            val users = userService.findAll(limit, offset)
            val total = userService.count()

            call.respond(
                mapOf(
                    "users" to users.map { UserResponse.from(it) },
                    "total" to total,
                    "limit" to limit,
                    "offset" to offset
                )
            )
        }

        // Get user by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

            val user = userService.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))

            call.respond(UserResponse.from(user))
        }

        // Update user
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

            val request = call.receive<UpdateUserRequest>()
            val updates = mutableMapOf<String, Any>()

            request.email?.let { updates["email"] = it }
            request.firstName?.let { updates["firstName"] = it }
            request.lastName?.let { updates["lastName"] = it }
            request.role?.let { updates["role"] = it }
            request.active?.let { updates["active"] = it }

            val user = userService.updateUser(id, updates)

            // Audit log
            auditService.logAction(
                userId = id,
                action = "UPDATE_USER",
                entityType = "User",
                entityId = id,
                newValue = updates.toString()
            )

            call.respond(UserResponse.from(user))
        }

        // Delete user
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

            val deleted = userService.delete(id)

            if (deleted) {
                // Audit log
                auditService.logAction(
                    userId = null,
                    action = "DELETE_USER",
                    entityType = "User",
                    entityId = id
                )

                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }

        // Authenticate
        post("/authenticate") {
            val request = call.receive<AuthRequest>()

            val user = userService.authenticate(request.email, request.password)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Invalid credentials")
                )

            // Audit log
            auditService.logAction(
                userId = user.id,
                action = "LOGIN",
                entityType = "User",
                entityId = user.id
            )

            call.respond(
                mapOf(
                    "user" to UserResponse.from(user),
                    "token" to "dummy-jwt-token" // In production, generate real JWT
                )
            )
        }
    }
}

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole? = null
)

data class UpdateUserRequest(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: UserRole? = null,
    val active: Boolean? = null
)

data class AuthRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val active: Boolean,
    val emailVerified: Boolean,
    val lastLoginAt: String?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(user: com.korm.examples.enterprise.models.User): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role.name,
                active = user.active,
                emailVerified = user.emailVerified,
                lastLoginAt = user.lastLoginAt?.toString(),
                createdAt = user.createdAt.toString(),
                updatedAt = user.updatedAt.toString()
            )
        }
    }
}
