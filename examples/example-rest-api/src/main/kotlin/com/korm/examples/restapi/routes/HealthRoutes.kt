// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/routes/HealthRoutes.kt

package com.korm.examples.restapi.routes

import com.korm.dsl.core.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes(database: Database) {
    route("/health") {
        get {
            try {
                database.transaction {
                    execute("SELECT 1")
                }

                call.respond(
                    mapOf(
                        "status" to "UP",
                        "database" to "connected"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.ServiceUnavailable,
                    mapOf(
                        "status" to "DOWN",
                        "database" to "disconnected",
                        "error" to e.message
                    )
                )
            }
        }
    }
}
