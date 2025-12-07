// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/routes/OrderRoutes.kt

package com.korm.examples.restapi.routes

import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.services.OrderService
import com.korm.examples.restapi.NotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(orderService: OrderService) {
    route("/orders") {
        // Create order
        post {
            val request = call.receive<CreateOrderRequest>()
            val order = orderService.createOrder(request)
            call.respond(HttpStatusCode.Created, order)
        }

        // Get order by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val order = orderService.findById(id)
                ?: throw NotFoundException("Order not found")

            call.respond(order)
        }

        // Get orders by user
        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

            val orders = orderService.findByUserId(userId, limit)
            call.respond(orders)
        }

        // Update order status
        patch("/{id}/status") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val request = call.receive<Map<String, String>>()
            val statusStr = request["status"]
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Status required")

            val status = try {
                OrderStatus.valueOf(statusStr.uppercase())
            } catch (e: IllegalArgumentException) {
                return@patch call.respond(HttpStatusCode.BadRequest, "Invalid status")
            }

            val order = orderService.updateStatus(id, status)
            call.respond(order)
        }

        // Cancel order
        post("/{id}/cancel") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val order = orderService.cancelOrder(id)
            call.respond(order)
        }
    }
}
