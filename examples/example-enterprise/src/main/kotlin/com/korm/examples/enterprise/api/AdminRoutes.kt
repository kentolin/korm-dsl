// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/AdminRoutes.kt

package com.korm.examples.enterprise.api

import com.korm.dsl.cache.Cache
import com.korm.dsl.monitoring.QueryMonitor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.adminRoutes(
    cache: Cache<String, Any>,
    queryMonitor: QueryMonitor
) {
    route("/admin") {
        // Cache management
        get("/cache/stats") {
            val stats = cache.stats()
            call.respond(stats)
        }

        post("/cache/clear") {
            cache.clear()
            call.respond(HttpStatusCode.NoContent)
        }

        // Query monitoring
        post("/queries/clear-stats") {
            queryMonitor.clear()
            call.respond(HttpStatusCode.NoContent)
        }

        // System info
        get("/system") {
            val runtime = Runtime.getRuntime()
            val info = mapOf(
                "availableProcessors" to runtime.availableProcessors(),
                "freeMemory" to runtime.freeMemory(),
                "totalMemory" to runtime.totalMemory(),
                "maxMemory" to runtime.maxMemory(),
                "usedMemory" to (runtime.totalMemory() - runtime.freeMemory())
            )
            call.respond(info)
        }
    }
}
