// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/services/ReportingService.kt

package com.korm.examples.enterprise.services

import com.korm.dsl.core.Database
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.examples.enterprise.models.*
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDateTime

class ReportingService(
    private val database: Database,
    private val queryMonitor: QueryMonitor
) {

    data class SalesReport(
        val totalOrders: Long,
        val totalRevenue: BigDecimal,
        val averageOrderValue: BigDecimal,
        val topProducts: List<ProductSales>,
        val salesByStatus: Map<OrderStatus, Long>
    )

    data class ProductSales(
        val productId: Long,
        val productName: String,
        val quantitySold: Long,
        val revenue: BigDecimal
    )

    data class UserActivity(
        val userId: Long,
        val username: String,
        val orderCount: Long,
        val totalSpent: BigDecimal,
        val lastOrderDate: LocalDateTime?
    )

    fun generateSalesReport(startDate: Long, endDate: Long): SalesReport {
        val startTime = System.nanoTime()

        val report = database.transaction {
            // Total orders and revenue
            val totalSql = """
                SELECT
                    COUNT(*) as total_orders,
                    COALESCE(SUM(total_amount), 0) as total_revenue,
                    COALESCE(AVG(total_amount), 0) as avg_order_value
                FROM orders
                WHERE placed_at BETWEEN ? AND ?
            """.trimIndent()

            var totalOrders = 0L
            var totalRevenue = BigDecimal.ZERO
            var avgOrderValue = BigDecimal.ZERO

            getConnection().use { conn ->
                conn.prepareStatement(totalSql).use { stmt ->
                    stmt.setLong(1, startDate)
                    stmt.setLong(2, endDate)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            totalOrders = rs.getLong("total_orders")
                            totalRevenue = rs.getBigDecimal("total_revenue")
                            avgOrderValue = rs.getBigDecimal("avg_order_value")
                        }
                    }
                }
            }

            // Top products
            val topProductsSql = """
                SELECT
                    p.id,
                    p.name,
                    SUM(oi.quantity) as quantity_sold,
                    SUM(oi.total_price) as revenue
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                JOIN orders o ON oi.order_id = o.id
                WHERE o.placed_at BETWEEN ? AND ?
                GROUP BY p.id, p.name
                ORDER BY revenue DESC
                LIMIT 10
            """.trimIndent()

            val topProducts = mutableListOf<ProductSales>()
            getConnection().use { conn ->
                conn.prepareStatement(topProductsSql).use { stmt ->
                    stmt.setLong(1, startDate)
                    stmt.setLong(2, endDate)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            topProducts.add(
                                ProductSales(
                                    productId = rs.getLong("id"),
                                    productName = rs.getString("name"),
                                    quantitySold = rs.getLong("quantity_sold"),
                                    revenue = rs.getBigDecimal("revenue")
                                )
                            )
                        }
                    }
                }
            }

            // Sales by status
            val statusSql = """
                SELECT status, COUNT(*) as count
                FROM orders
                WHERE placed_at BETWEEN ? AND ?
                GROUP BY status
            """.trimIndent()

            val salesByStatus = mutableMapOf<OrderStatus, Long>()
            getConnection().use { conn ->
                conn.prepareStatement(statusSql).use { stmt ->
                    stmt.setLong(1, startDate)
                    stmt.setLong(2, endDate)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            val status = OrderStatus.valueOf(rs.getString("status"))
                            val count = rs.getLong("count")
                            salesByStatus[status] = count
                        }
                    }
                }
            }

            SalesReport(
                totalOrders = totalOrders,
                totalRevenue = totalRevenue,
                averageOrderValue = avgOrderValue,
                topProducts = topProducts,
                salesByStatus = salesByStatus
            )
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        queryMonitor.recordQuery("Generate sales report", emptyMap(), duration)

        return report
    }

    fun getTopCustomers(limit: Int = 20): List<UserActivity> {
        val startTime = System.nanoTime()

        val customers = database.transaction {
            val sql = """
                SELECT
                    u.id,
                    u.username,
                    COUNT(o.id) as order_count,
                    COALESCE(SUM(o.total_amount), 0) as total_spent,
                    MAX(o.placed_at) as last_order_date
                FROM users u
                LEFT JOIN orders o ON u.id = o.user_id
                GROUP BY u.id, u.username
                HAVING COUNT(o.id) > 0
                ORDER BY total_spent DESC
                LIMIT ?
            """.trimIndent()

            val results = mutableListOf<UserActivity>()
            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, limit)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            results.add(
                                UserActivity(
                                    userId = rs.getLong("id"),
                                    username = rs.getString("username"),
                                    orderCount = rs.getLong("order_count"),
                                    totalSpent = rs.getBigDecimal("total_spent"),
                                    lastOrderDate = rs.getLong("last_order_date").takeIf { it > 0 }?.let {
                                        LocalDateTime.ofInstant(
                                            java.time.Instant.ofEpochMilli(it),
                                            java.time.ZoneId.systemDefault()
                                        )
                                    }
                                )
                            )
                        }
                    }
                }
            }
            results
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        queryMonitor.recordQuery("Get top customers", emptyMap(), duration)

        return customers
    }

    fun getInventoryReport(): List<InventoryReport> {
        return database.transaction {
            val sql = """
                SELECT
                    id,
                    sku,
                    name,
                    category,
                    stock,
                    reorder_level,
                    price,
                    cost_price,
                    (price - cost_price) as profit_margin
                FROM products
                WHERE active = true
                ORDER BY stock ASC
            """.trimIndent()

            val results = mutableListOf<InventoryReport>()
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            results.add(
                                InventoryReport(
                                    productId = rs.getLong("id"),
                                    sku = rs.getString("sku"),
                                    name = rs.getString("name"),
                                    category = rs.getString("category"),
                                    stock = rs.getInt("stock"),
                                    reorderLevel = rs.getInt("reorder_level"),
                                    price = rs.getBigDecimal("price"),
                                    costPrice = rs.getBigDecimal("cost_price"),
                                    profitMargin = rs.getBigDecimal("profit_margin"),
                                    needsReorder = rs.getInt("stock") <= rs.getInt("reorder_level")
                                )
                            )
                        }
                    }
                }
            }
            results
        }
    }

    data class InventoryReport(
        val productId: Long,
        val sku: String,
        val name: String,
        val category: String,
        val stock: Int,
        val reorderLevel: Int,
        val price: BigDecimal,
        val costPrice: BigDecimal,
        val profitMargin: BigDecimal,
        val needsReorder: Boolean
    )
}
