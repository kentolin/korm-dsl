// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/examples/ManyToManyExample.kt

package com.korm.examples.relationships.examples

import com.korm.examples.relationships.repositories.BookRepository
import com.korm.examples.relationships.repositories.CategoryRepository

class ManyToManyExample(
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository
) {
    fun run() {
        println("=== Many-to-Many Relationship Example ===")

        // Create categories
        val fantasy = categoryRepository.create(
            name = "Fantasy",
            description = "Fantasy and magical worlds"
        )
        val youngAdult = categoryRepository.create(
            name = "Young Adult",
            description = "Books for young adult readers"
        )
        val adventure = categoryRepository.create(
            name = "Adventure",
            description = "Adventure and action"
        )

        println("Created categories: Fantasy, Young Adult, Adventure")

        // Get a book
        val book = bookRepository.findById(1)
        book?.let {
            println("\nAdding categories to book: ${it.title}")

            // Add multiple categories to the book
            bookRepository.addCategory(it.id, fantasy.id)
            bookRepository.addCategory(it.id, youngAdult.id)
            bookRepository.addCategory(it.id, adventure.id)

            // Fetch book with categories
            val bookWithCategories = bookRepository.findWithCategories(it.id)
            bookWithCategories?.let { bwc ->
                println("Book: ${bwc.book.title}")
                println("Categories (${bwc.categories.size}):")
                bwc.categories.forEach { category ->
                    println("  - ${category.name}")
                }
            }
        }
    }
}
