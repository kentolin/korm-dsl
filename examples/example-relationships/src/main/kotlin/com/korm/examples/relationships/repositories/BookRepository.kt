// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/repositories/BookRepository.kt

package com.korm.examples.relationships.repositories

import com.korm.dsl.core.Database
import com.korm.examples.relationships.models.*
import java.sql.ResultSet

class BookRepository(private val database: Database) {

    fun create(title: String, isbn: String, authorId: Long, publishedYear: Int, pages: Int? = null): Book {
        val bookId = database.transaction {
            val query = insertInto(Books) {
                it[Books.title] = title
                it[Books.isbn] = isbn
                it[Books.authorId] = authorId
                it[Books.publishedYear] = publishedYear
                pages?.let { p -> it[Books.pages] = p }
            }
            insert(query)
        }

        return findById(bookId)!!
    }

    fun findById(id: Long): Book? {
        return database.transaction {
            val query = from(Books).where(Books.id eq id)
            val results = select(query, ::mapBook)
            results.firstOrNull()
        }
    }

    fun findByAuthor(authorId: Long): List<Book> {
        return database.transaction {
            val query = from(Books)
                .where(Books.authorId eq authorId)
                .orderBy(Books.publishedYear, "DESC")
            select(query, ::mapBook)
        }
    }

    fun findWithAuthor(bookId: Long): BookWithAuthor? {
        return database.transaction {
            val book = findById(bookId) ?: return@transaction null

            val authorQuery = from(Authors).where(Authors.id eq book.authorId)
            val author = select(authorQuery, ::mapAuthor).firstOrNull() ?: return@transaction null

            BookWithAuthor(book, author)
        }
    }

    fun addCategory(bookId: Long, categoryId: Long) {
        database.transaction {
            val query = insertInto(BookCategories) {
                it[BookCategories.bookId] = bookId
                it[BookCategories.categoryId] = categoryId
            }
            insert(query)
        }
    }

    fun findWithCategories(bookId: Long): BookWithCategories? {
        return database.transaction {
            val book = findById(bookId) ?: return@transaction null

            // Join query to get categories
            val sql = """
                SELECT c.* FROM categories c
                INNER JOIN book_categories bc ON c.id = bc.category_id
                WHERE bc.book_id = ?
            """.trimIndent()

            val categories = mutableListOf<Category>()
            val connection = getConnection()
            connection.prepareStatement(sql).use { stmt ->
                stmt.setLong(1, bookId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    categories.add(mapCategory(rs))
                }
            }

            BookWithCategories(book, categories)
        }
    }

    private fun mapBook(rs: ResultSet) = Book(
        id = rs.getLong("id"),
        title = rs.getString("title"),
        isbn = rs.getString("isbn"),
        authorId = rs.getLong("author_id"),
        publishedYear = rs.getInt("published_year"),
        pages = rs.getInt("pages").takeIf { !rs.wasNull() },
        createdAt = rs.getLong("created_at")
    )

    private fun mapAuthor(rs: ResultSet) = Author(
        id = rs.getLong("id"),
        name = rs.getString("name"),
        email = rs.getString("email"),
        bio = rs.getString("bio"),
        createdAt = rs.getLong("created_at")
    )

    private fun mapCategory(rs: ResultSet) = Category(
        id = rs.getLong("id"),
        name = rs.getString("name"),
        description = rs.getString("description"),
        createdAt = rs.getLong("created_at")
    )
}
