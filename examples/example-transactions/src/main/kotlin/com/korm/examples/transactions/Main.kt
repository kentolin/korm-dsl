// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/Main.kt

package com.korm.examples.transactions

import com.korm.dsl.core.Database
import com.korm.examples.transactions.examples.*
import com.korm.examples.transactions.models.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    logger.info("Starting KORM Transactions Example")

    // Initialize database
    val database = Database.connect(
        url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/korm_transactions",
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "password"
    )

    try {
        // Create tables
        logger.info("Creating tables...")
        database.dropTables(TransferLogs, Accounts, Users)
        database.createTables(Users, Accounts, TransferLogs)

        println("\n========================================")
        println("KORM DSL - Transaction Examples")
        println("========================================\n")

        // Run examples
        BasicTransactionExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        RollbackExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        IsolationLevelExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        NestedTransactionExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        MoneyTransferExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        DeadlockExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        SavepointExample(database).run()
        println("\n" + "=".repeat(40) + "\n")

        BatchTransactionExample(database).run()

        println("\n========================================")
        println("All transaction examples completed!")
        println("========================================\n")

    } catch (e: Exception) {
        logger.error("Error running examples", e)
        throw e
    } finally {
        database.close()
    }
}
