package com.korm.examples.relationships

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.query.*
import com.korm.dsl.schema.create
import com.korm.dsl.schema.drop
import com.korm.examples.relationships.models.*

fun main() {
    println("=== KORM DSL Relationships & JOIN Example ===\n")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    val db = Database(H2Dialect, pool)

    try {
        // Create tables
        println("1. Creating tables...")
        Authors.create(db)
        Books.create(db)
        Categories.create(db)
        BookCategories.create(db)
        println("✓ Tables created\n")

        // Insert authors
        println("2. Inserting authors...")
        Authors.insert(db)
            .set(Authors.name, "J.K. Rowling")
            .set(Authors.email, "jk@example.com")
            .set(Authors.country, "UK")
            .execute()

        Authors.insert(db)
            .set(Authors.name, "George Orwell")
            .set(Authors.email, "george@example.com")
            .set(Authors.country, "UK")
            .execute()

        Authors.insert(db)
            .set(Authors.name, "Haruki Murakami")
            .set(Authors.email, "haruki@example.com")
            .set(Authors.country, "Japan")
            .execute()
        println("✓ Inserted 3 authors\n")

        // Insert books
        println("3. Inserting books...")
        Books.insert(db)
            .set(Books.title, "Harry Potter and the Philosopher's Stone")
            .set(Books.isbn, "978-0747532699")
            .set(Books.authorId, 1)
            .set(Books.publishYear, 1997)
            .set(Books.price, 19.99)
            .execute()

        Books.insert(db)
            .set(Books.title, "1984")
            .set(Books.isbn, "978-0451524935")
            .set(Books.authorId, 2)
            .set(Books.publishYear, 1949)
            .set(Books.price, 15.99)
            .execute()

        Books.insert(db)
            .set(Books.title, "Animal Farm")
            .set(Books.isbn, "978-0451526342")
            .set(Books.authorId, 2)
            .set(Books.publishYear, 1945)
            .set(Books.price, 12.99)
            .execute()

        Books.insert(db)
            .set(Books.title, "Norwegian Wood")
            .set(Books.isbn, "978-0375704024")
            .set(Books.authorId, 3)
            .set(Books.publishYear, 1987)
            .set(Books.price, 16.99)
            .execute()
        println("✓ Inserted 4 books\n")

        // Insert categories
        println("4. Inserting categories...")
        Categories.insert(db)
            .set(Categories.name, "Fantasy")
            .set(Categories.description, "Fantasy and magical fiction")
            .execute()

        Categories.insert(db)
            .set(Categories.name, "Dystopian")
            .set(Categories.description, "Dystopian fiction")
            .execute()

        Categories.insert(db)
            .set(Categories.name, "Literary Fiction")
            .set(Categories.description, "Literary and contemporary fiction")
            .execute()
        println("✓ Inserted 3 categories\n")

        // Link books to categories
        println("5. Linking books to categories...")
        BookCategories.insert(db).set(BookCategories.bookId, 1).set(BookCategories.categoryId, 1).execute()
        BookCategories.insert(db).set(BookCategories.bookId, 2).set(BookCategories.categoryId, 2).execute()
        BookCategories.insert(db).set(BookCategories.bookId, 3).set(BookCategories.categoryId, 2).execute()
        BookCategories.insert(db).set(BookCategories.bookId, 4).set(BookCategories.categoryId, 3).execute()
        println("✓ Links created\n")

        // Example 1: INNER JOIN - Books with their authors
        println("6. INNER JOIN: Books with their authors")
        println("-".repeat(70))
        val booksWithAuthors = Books.select(db)
            .innerJoin(Authors) { books, _ ->
                books.authorId to Authors.id
            }
            .execute { rs ->
                BookWithAuthor(
                    bookId = rs.getInt("id"),
                    bookTitle = rs.getString("title"),
                    isbn = rs.getString("isbn"),
                    publishYear = rs.getObject("publish_year") as? Int,
                    price = rs.getObject("price") as? Double,
                    authorId = rs.getInt("author_id"),
                    authorName = rs.getString("name"),
                    authorEmail = rs.getString("email"),
                    authorCountry = rs.getString("country")
                )
            }

        booksWithAuthors.forEach { book ->
            println("  '${book.bookTitle}' by ${book.authorName} (${book.authorCountry})")
            println("    ISBN: ${book.isbn}, Year: ${book.publishYear}, Price: $${book.price}")
        }
        println()

        // Example 2: Filter JOIN results
        println("7. INNER JOIN with WHERE: Books by UK authors")
        println("-".repeat(70))
        val ukBooks = Books.select(db)
            .innerJoin(Authors) { books, _ ->
                books.authorId to Authors.id
            }
            .where(Authors.country, "UK")
            .execute { rs ->
                mapOf(
                    "title" to rs.getString("title"),
                    "author" to rs.getString("name"),
                    "year" to rs.getObject("publish_year")
                )
            }

        ukBooks.forEach { book ->
            println("  ${book["title"]} by ${book["author"]} (${book["year"]})")
        }
        println()

        // Example 3: Multiple JOINs - Books with authors and categories
        println("8. Multiple JOINs: Books with authors and categories")
        println("-".repeat(70))

        // Using joinOn for direct column specification (works with complex joins)
        // Select specific columns with aliases to avoid naming conflicts
        val booksWithCategories = Books.select(db)
            .select(Books.title, Authors.name, Categories.name)
            .innerJoinOn(Authors, Books.authorId, Authors.id)
            .innerJoinOn(BookCategories, Books.id, BookCategories.bookId)
            .innerJoinOn(Categories, BookCategories.categoryId, Categories.id)
            .execute { rs ->
                mapOf(
                    "title" to rs.getString("title"),
                    "author" to rs.getString("name"), // First name (from Authors)
                    "category" to rs.getString("categories_name") // Aliased as categories_name
                )
            }

        booksWithCategories.forEach { row ->
            println("  ${row["title"]} by ${row["author"]} [${row["category"]}]")
        }
        println()

        // Example 4: LEFT JOIN - All authors with their book count
        println("9. Counting books per author")
        println("-".repeat(70))

        // First, let's manually query to show the concept
        val authorsQuery = Authors.select(db).execute { rs ->
            Author(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                country = rs.getString("country")
            )
        }

        authorsQuery.forEach { author ->
            val bookCount = Books.select(db)
                .where(Books.authorId, author.id)
                .execute { rs -> rs.getInt("id") }
                .size

            println("  ${author.name}: $bookCount book(s)")
        }
        println()

        // Example 5: Order results from JOIN
        println("10. Books with authors, ordered by price")
        println("-".repeat(70))
        val booksByPrice = Books.select(db)
            .innerJoin(Authors) { books, _ ->
                books.authorId to Authors.id
            }
            .orderBy(Books.price, asc = false)
            .execute { rs ->
                mapOf(
                    "title" to rs.getString("title"),
                    "author" to rs.getString("name"),
                    "price" to rs.getObject("price")
                )
            }

        booksByPrice.forEach { book ->
            println("  ${book["title"]} by ${book["author"]} - $${book["price"]}")
        }
        println()

        println("✓ JOIN examples completed successfully!")

    } finally {
        db.close()
    }

    println("\n=== Example completed ===")
}