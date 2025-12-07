// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/HealthRoutes.kt

package com.korm.examples.enterprise.api

import com.korm.dsl.monitoring.HealthCheckRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes(healthRegistry: HealthCheckRegistry) {
    route("/health") {
        get {
            val overall = healthRegistry.getOverallStatus()

            val statusCode = if (overall.isHealthy()) {
                HttpStatusCode.OK
            } else {
                HttpStatusCode.ServiceUnavailable
            }

            call.respond(statusCode, overall)
        }

        get("/detailed") {
            val results = healthRegistry.runChecks()
            call.respond(results)
        }
    }
}
