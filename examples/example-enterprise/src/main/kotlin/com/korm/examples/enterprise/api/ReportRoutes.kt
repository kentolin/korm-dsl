// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/ReportRoutes.kt

package com.korm.examples.enterprise.api

import com.korm.examples.enterprise.services.ReportingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.time.ZoneId

fun Route.reportRoutes(reportingService: ReportingService) {
    route("/api/reports") {
        // Sales report
        get("/sales") {
            val startDateStr = call.request.queryParameters["startDate"]
            val endDateStr = call.request.queryParameters["endDate"]

            val startDate = startDateStr?.let {
                LocalDateTime.parse(it).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } ?: (System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) // Default: 30 days ago

            val endDate = endDateStr?.let {
                LocalDateTime.parse(it).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } ?: System.currentTimeMillis()

            val report = reportingService.generateSalesReport(startDate, endDate)
            call.respond(report)
        }

        // Top customers
        get("/customers/top") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val customers = reportingService.getTopCustomers(limit)
            call.respond(customers)
        }

        // Inventory report
        get("/inventory") {
            val inventory = reportingService.getInventoryReport()
            call.respond(inventory)
        }
    }
}
