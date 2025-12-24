package com.korm.examples.aggregates

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.query.*
import com.korm.dsl.schema.create
import com.korm.dsl.expressions.*
import com.korm.examples.aggregates.models.*

fun main() {
    println("=== KORM DSL GROUP BY & Aggregates Example ===\n")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    val db = Database(H2Dialect, pool)

    try {
        // Create tables
        println("1. Creating tables...")
        Customers.create(db)
        Products.create(db)
        Orders.create(db)
        println("✓ Tables created\n")

        // Insert sample data
        insertSampleData(db)

        // Example 1: COUNT - Total number of orders
        println("5. COUNT: Total number of orders")
        println("-".repeat(70))
        val totalOrders = Orders.select(db)
            .selectAggregate(count(alias = "total"))
            .execute { rs ->
                rs.getInt("total")
            }
        println("  Total orders: ${totalOrders.first()}\n")

        // Example 2: COUNT DISTINCT - Number of unique customers who placed orders
        println("6. COUNT DISTINCT: Unique customers with orders")
        println("-".repeat(70))
        val uniqueCustomers = Orders.select(db)
            .selectAggregate(count(Orders.customerId, distinct = true, alias = "unique_customers"))
            .execute { rs ->
                rs.getInt("unique_customers")
            }
        println("  Unique customers: ${uniqueCustomers.first()}\n")

        // Example 3: SUM - Total revenue from all orders
        println("7. SUM: Total revenue")
        println("-".repeat(70))
        val totalRevenue = Orders.select(db)
            .selectAggregate(sum(Orders.totalAmount, alias = "total_revenue"))
            .execute { rs ->
                rs.getDouble("total_revenue")
            }
        println("  Total revenue: $${"%.2f".format(totalRevenue.first())}\n")

        // Example 4: AVG - Average order value
        println("8. AVG: Average order value")
        println("-".repeat(70))
        val avgOrderValue = Orders.select(db)
            .selectAggregate(avg(Orders.totalAmount, alias = "avg_order"))
            .execute { rs ->
                rs.getDouble("avg_order")
            }
        println("  Average order value: $${"%.2f".format(avgOrderValue.first())}\n")

        // Example 5: MIN and MAX - Price range
        println("9. MIN & MAX: Product price range")
        println("-".repeat(70))
        val priceRange = Products.select(db)
            .selectAggregate(
                min(Products.price, alias = "min_price"),
                max(Products.price, alias = "max_price")
            )
            .execute { rs ->
                mapOf(
                    "min" to rs.getDouble("min_price"),
                    "max" to rs.getDouble("max_price")
                )
            }
        val range = priceRange.first()
        println("  Price range: $${"%.2f".format(range["min"])} - $${"%.2f".format(range["max"])}\n")

        // Example 6: GROUP BY - Orders count per customer
        println("10. GROUP BY: Orders per customer")
        println("-".repeat(70))
        val ordersPerCustomer = Orders.select(db)
            .selectAggregate(count(alias = "order_count"))
            .groupBy(Orders.customerId)
            .execute { rs ->
                mapOf(
                    "customer_id" to rs.getInt("customer_id"),
                    "order_count" to rs.getInt("order_count")
                )
            }
        ordersPerCustomer.forEach { row ->
            println("  Customer ${row["customer_id"]}: ${row["order_count"]} order(s)")
        }
        println()

        // Example 7: GROUP BY with multiple aggregates
        println("11. GROUP BY: Customer statistics")
        println("-".repeat(70))
        val customerStats = Orders.select(db)
            .selectAggregate(
                count(alias = "num_orders"),
                sum(Orders.totalAmount, alias = "total_spent"),
                avg(Orders.totalAmount, alias = "avg_order_value")
            )
            .groupBy(Orders.customerId)
            .execute { rs ->
                mapOf(
                    "customer_id" to rs.getInt("customer_id"),
                    "num_orders" to rs.getInt("num_orders"),
                    "total_spent" to rs.getDouble("total_spent"),
                    "avg_order" to rs.getDouble("avg_order_value")
                )
            }
        customerStats.forEach { row ->
            println("  Customer ${row["customer_id"]}:")
            println("    Orders: ${row["num_orders"]}")
            println("    Total spent: $${"%.2f".format(row["total_spent"])}")
            println("    Avg order: $${"%.2f".format(row["avg_order"])}")
        }
        println()

        // Example 8: GROUP BY with WHERE - Orders per status (only completed/pending)
        println("12. GROUP BY with WHERE: Orders by status")
        println("-".repeat(70))
        val ordersByStatus = Orders.select(db)
            .selectAggregate(count(alias = "count"))
            .where(Orders.status, "completed")
            .groupBy(Orders.status)
            .execute { rs ->
                mapOf(
                    "status" to rs.getString("status"),
                    "count" to rs.getInt("count")
                )
            }
        ordersByStatus.forEach { row ->
            println("  ${row["status"]}: ${row["count"]} order(s)")
        }
        println()

        // Example 9: GROUP BY products - Sales per product
        println("13. GROUP BY: Sales per product")
        println("-".repeat(70))
        val salesPerProduct = Orders.select(db)
            .selectAggregate(
                sum(Orders.quantity, alias = "total_quantity"),
                sum(Orders.totalAmount, alias = "total_revenue")
            )
            .groupBy(Orders.productId)
            .execute { rs ->
                mapOf(
                    "product_id" to rs.getInt("product_id"),
                    "quantity" to rs.getInt("total_quantity"),
                    "revenue" to rs.getDouble("total_revenue")
                )
            }
        salesPerProduct.forEach { row ->
            println("  Product ${row["product_id"]}:")
            println("    Units sold: ${row["quantity"]}")
            println("    Revenue: $${"%.2f".format(row["revenue"])}")
        }
        println()

        // Example 10: HAVING - Customers with more than 2 orders
        println("14. HAVING: High-frequency customers (>2 orders)")
        println("-".repeat(70))
        val highFrequencyCustomers = Orders.select(db)
            .selectAggregate(count(alias = "order_count"))
            .groupBy(Orders.customerId)
            .having(count(), ">", 2)
            .execute { rs ->
                mapOf(
                    "customer_id" to rs.getInt("customer_id"),
                    "order_count" to rs.getInt("order_count")
                )
            }
        if (highFrequencyCustomers.isEmpty()) {
            println("  No customers with more than 2 orders")
        } else {
            highFrequencyCustomers.forEach { row ->
                println("  Customer ${row["customer_id"]}: ${row["order_count"]} orders")
            }
        }
        println()

        // Example 11: Multiple aggregates with complex grouping
        println("15. Advanced: Product category analytics")
        println("-".repeat(70))
        val categoryStats = Products.select(db)
            .selectAggregate(
                count(alias = "num_products"),
                avg(Products.price, alias = "avg_price"),
                min(Products.price, alias = "min_price"),
                max(Products.price, alias = "max_price"),
                sum(Products.stock, alias = "total_stock")
            )
            .groupBy(Products.category)
            .execute { rs ->
                mapOf(
                    "category" to rs.getString("category"),
                    "count" to rs.getInt("num_products"),
                    "avg_price" to rs.getDouble("avg_price"),
                    "min_price" to rs.getDouble("min_price"),
                    "max_price" to rs.getDouble("max_price"),
                    "stock" to rs.getInt("total_stock")
                )
            }
        categoryStats.forEach { row ->
            println("  ${row["category"]}:")
            println("    Products: ${row["count"]}")
            println("    Price range: $${"%.2f".format(row["min_price"])} - $${"%.2f".format(row["max_price"])}")
            println("    Avg price: $${"%.2f".format(row["avg_price"])}")
            println("    Total stock: ${row["stock"]}")
        }
        println()

        println("✓ All aggregate examples completed successfully!")

    } finally {
        db.close()
    }

    println("\n=== Example completed ===")
}

fun insertSampleData(db: Database) {
    // Insert customers
    println("2. Inserting customers...")
    listOf(
        Triple("Alice Johnson", "New York", "USA"),
        Triple("Bob Smith", "London", "UK"),
        Triple("Charlie Davis", "Paris", "France"),
        Triple("Diana Wilson", "New York", "USA")
    ).forEach { (name, city, country) ->
        Customers.insert(db)
            .set(Customers.name, name)
            .set(Customers.city, city)
            .set(Customers.country, country)
            .execute()
    }
    println("✓ Inserted 4 customers\n")

    // Insert products
    println("3. Inserting products...")
    listOf(
        Triple("Laptop", "Electronics", 999.99),
        Triple("Mouse", "Electronics", 29.99),
        Triple("Keyboard", "Electronics", 79.99),
        Triple("Desk", "Furniture", 299.99),
        Triple("Chair", "Furniture", 199.99),
        Triple("Monitor", "Electronics", 349.99)
    ).forEachIndexed { index, (name, category, price) ->
        Products.insert(db)
            .set(Products.name, name)
            .set(Products.category, category)
            .set(Products.price, price)
            .set(Products.stock, (index + 1) * 10)
            .execute()
    }
    println("✓ Inserted 6 products\n")

    // Insert orders
    println("4. Inserting orders...")
    listOf(
        // (customerId, productId, quantity, status, date)
        Tuple5(1, 1, 1, "completed", "2024-01-15"),
        Tuple5(1, 2, 2, "completed", "2024-01-16"),
        Tuple5(1, 3, 1, "completed", "2024-01-20"),
        Tuple5(2, 1, 1, "completed", "2024-01-18"),
        Tuple5(2, 4, 2, "completed", "2024-01-19"),
        Tuple5(3, 5, 1, "pending", "2024-01-21"),
        Tuple5(3, 6, 1, "completed", "2024-01-22"),
        Tuple5(4, 2, 3, "completed", "2024-01-23")
    ).forEach { (customerId, productId, quantity, status, date) ->
        // Calculate total based on product price
        val price = when(productId) {
            1 -> 999.99
            2 -> 29.99
            3 -> 79.99
            4 -> 299.99
            5 -> 199.99
            6 -> 349.99
            else -> 0.0
        }

        Orders.insert(db)
            .set(Orders.customerId, customerId)
            .set(Orders.productId, productId)
            .set(Orders.quantity, quantity)
            .set(Orders.totalAmount, price * quantity)
            .set(Orders.orderDate, date)
            .set(Orders.status, status)
            .execute()
    }
    println("✓ Inserted 8 orders\n")
}

// Helper class for tuples
data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)