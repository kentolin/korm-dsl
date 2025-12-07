// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/examples/EagerLoadingExample.kt

package com.korm.examples.relationships.examples

import com.korm.examples.relationships.repositories.AuthorRepository
import com.korm.examples.relationships.repositories.BookRepository

class EagerLoadingExample(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {
    fun run() {
        println("=== Eager Loading Example ===")

        // Load author with all books in one query (eager loading)
        val authorWithBooks = authorRepository.findWithBooks(1)

        authorWithBooks?.let {
            println("Loaded author: ${it.author.name}")
            println("Books loaded eagerly: ${it.books.size}")
            it.books.forEach { book ->
                println("  - ${book.title}")
            }
        }
    }
}
