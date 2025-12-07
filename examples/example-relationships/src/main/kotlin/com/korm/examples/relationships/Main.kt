// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/Main.kt

package com.korm.examples.relationships

import com.korm.dsl.core.Database
import com.korm.examples.relationships.examples.*
import com.korm.examples.relationships.models.*
import com.korm.examples.relationships.repositories.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    logger.info("Starting KORM DSL Relationships Example")

    val database = Database.connect(
        url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/korm_relationships",
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER") ?: "postgres",
        password = System.getenv("DB_PASSWORD") ?: "password"
    )

    try {
        // Create tables
        logger.info("Creating tables...")
        database.dropTables(BookCategories, Books, Categories, Authors)
        database.createTables(Authors, Categories, Books, BookCategories)

        // Initialize repositories
        val authorRepository = AuthorRepository(database)
        val bookRepository = BookRepository(database)
        val categoryRepository = CategoryRepository(database)

        println("\n========================================")
        println("KORM DSL - Relationships Examples")
        println("========================================\n")

        OneToManyExample(authorRepository, bookRepository).run()
        println()

        ManyToManyExample(bookRepository, categoryRepository).run()
        println()

        EagerLoadingExample(authorRepository, bookRepository).run()
        println()

        LazyLoadingExample(authorRepository, bookRepository).run()

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
