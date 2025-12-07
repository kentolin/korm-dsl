// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/examples/LazyLoadingExample.kt

package com.korm.examples.relationships.examples

import com.korm.examples.relationships.repositories.AuthorRepository
import com.korm.examples.relationships.repositories.BookRepository

class LazyLoadingExample(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {
    fun run() {
        println("=== Lazy Loading Example ===")

        // Load author only
        val author = authorRepository.findById(1)
        author?.let {
            println("Loaded author: ${it.name}")

            // Load books later when needed (lazy loading)
            println("Now loading books...")
            val books = bookRepository.findByAuthor(it.id)
            println("Books loaded lazily: ${books.size}")
            books.forEach { book ->
                println("  - ${book.title}")
            }
        }
    }
}
