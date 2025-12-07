// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/Main.kt

package com.korm.examples.basic

import com.korm.dsl.core.Database
import com.korm.examples.basic.examples.*
import com.korm.examples.basic.models.Users
import com.korm.examples.basic.repositories.UserRepository
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    logger.info("Starting KORM DSL Basic Example")

    // Initialize database
    val database = Database.connect(
        url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/korm_basic",
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "password"
    )

    try {
        // Create tables
        logger.info("Creating tables...")
        database.dropTables(Users) // Clean start
        database.createTables(Users)

        // Initialize repository
        val userRepository = UserRepository(database)

        // Run examples
        println("\n========================================")
        println("KORM DSL - Basic CRUD Examples")
        println("========================================\n")

        CreateExample(userRepository).run()
        println()

        ReadExample(userRepository).run()
        println()

        UpdateExample(userRepository).run()
        println()

        QueryExample(userRepository).run()
        println()

        DeleteExample(userRepository).run()

        println("\n========================================")
        println("All examples completed successfully!")
        println("========================================\n")

    } catch (e: Exception) {
        logger.error("Error running examples", e)
        throw e
    } finally {
        database.close()
    }
}
