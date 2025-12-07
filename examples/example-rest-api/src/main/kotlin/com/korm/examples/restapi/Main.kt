// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/Main.kt

package com.korm.examples.restapi

import com.korm.dsl.core.Database
import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.routes.*
import com.korm.examples.restapi.services.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize database
    val database = Database.connect(
        url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/korm_rest_api",
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "password"
    )

    // Create tables
    database.createTables(Users, Products, Orders, OrderItems)

    // Initialize services
    val userService = UserService(database)
    val productService = ProductService(database)
    val orderService = OrderService(database)

    // Install plugins
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(cause.message ?: "Internal server error")
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message ?: "Not found"))
        }

        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Validation failed"))
        }
    }

    // Configure routing
    routing {
        route("/api") {
            userRoutes(userService)
            productRoutes(productService)
            orderRoutes(orderService)
            healthRoutes(database)
        }
    }

    logger.info("REST API server started on http://localhost:8080")
}

data class ErrorResponse(val error: String)
class NotFoundException(message: String) : Exception(message)
class ValidationException(message: String) : Exception(message)
