// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/repositories/AuthorRepository.kt

package com.korm.examples.relationships.repositories

import com.korm.dsl.core.Database
import com.korm.examples.relationships.models.*
import java.sql.ResultSet

class AuthorRepository(private val database: Database) {

    fun create(name: String, email: String, bio: String? = null): Author {
        val authorId = database.transaction {
            val query = insertInto(Authors) {
                it[Authors.name] = name
                it[Authors.email] = email
                bio?.let { b -> it[Authors.bio] = b }
            }
            insert(query)
        }

        return findById(authorId)!!
    }

    fun findById(id: Long): Author? {
        return database.transaction {
            val query = from(Authors).where(Authors.id eq id)
            val results = select(query, ::mapAuthor)
            results.firstOrNull()
        }
    }

    fun findAll(): List<Author> {
        return database.transaction {
            val query = from(Authors).orderBy(Authors.name)
            select(query, ::mapAuthor)
        }
    }

    fun findWithBooks(authorId: Long): AuthorWithBooks? {
        return database.transaction {
            val author = findById(authorId) ?: return@transaction null

            val booksQuery = from(Books).where(Books.authorId eq authorId)
            val books = select(booksQuery, ::mapBook)

            AuthorWithBooks(author, books)
        }
    }

    private fun mapAuthor(rs: ResultSet) = Author(
        id = rs.getLong("id"),
        name = rs.getString("name"),
        email = rs.getString("email"),
        bio = rs.getString("bio"),
        createdAt = rs.getLong("created_at")
    )

    private fun mapBook(rs: ResultSet) = Book(
        id = rs.getLong("id"),
        title = rs.getString("title"),
        isbn = rs.getString("isbn"),
        authorId = rs.getLong("author_id"),
        publishedYear = rs.getInt("published_year"),
        pages = rs.getInt("pages").takeIf { !rs.wasNull() },
        createdAt = rs.getLong("created_at")
    )
}
