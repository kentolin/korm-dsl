// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/routes/UserRoutes.kt

package com.korm.examples.restapi.routes

import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.services.UserService
import com.korm.examples.restapi.NotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/users") {
        // Create user
        post {
            val request = call.receive<CreateUserRequest>()
            val user = userService.createUser(request)
            call.respond(HttpStatusCode.Created, UserResponse.from(user))
        }

        // Get all users
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
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val user = userService.findById(id)
                ?: throw NotFoundException("User not found")

            call.respond(UserResponse.from(user))
        }

        // Get user by username
        get("/username/{username}") {
            val username = call.parameters["username"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Username required")

            val user = userService.findByUsername(username)
                ?: throw NotFoundException("User not found")

            call.respond(UserResponse.from(user))
        }

        // Update user
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val request = call.receive<UpdateUserRequest>()
            val user = userService.update(id, request)
            call.respond(UserResponse.from(user))
        }

        // Delete user
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val deleted = userService.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                throw NotFoundException("User not found")
            }
        }
    }
}
