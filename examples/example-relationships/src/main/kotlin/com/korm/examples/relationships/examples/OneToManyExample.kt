// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/examples/OneToManyExample.kt

package com.korm.examples.relationships.examples

import com.korm.examples.relationships.repositories.AuthorRepository
import com.korm.examples.relationships.repositories.BookRepository

class OneToManyExample(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {
    fun run() {
        println("=== One-to-Many Relationship Example ===")

        // Create an author
        val author = authorRepository.create(
            name = "J.K. Rowling",
            email = "jk.rowling@example.com",
            bio = "British author, best known for the Harry Potter series"
        )
        println("Created author: ${author.name}")

        // Create books for this author
        val book1 = bookRepository.create(
            title = "Harry Potter and the Philosopher's Stone",
            isbn = "978-0439708180",
            authorId = author.id,
            publishedYear = 1997,
            pages = 309
        )
        println("Created book: ${book1.title}")

        val book2 = bookRepository.create(
            title = "Harry Potter and the Chamber of Secrets",
            isbn = "978-0439064873",
            authorId = author.id,
            publishedYear = 1998,
            pages = 341
        )
        println("Created book: ${book2.title}")

        // Fetch author with all books
        val authorWithBooks = authorRepository.findWithBooks(author.id)
        authorWithBooks?.let {
            println("\nAuthor: ${it.author.name}")
            println("Books (${it.books.size}):")
            it.books.forEach { book ->
                println("  - ${book.title} (${book.publishedYear})")
            }
        }
    }
}
