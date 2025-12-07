// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/repositories/CategoryRepository.kt

package com.korm.examples.relationships.repositories

import com.korm.dsl.core.Database
import com.korm.examples.relationships.models.Categories
import com.korm.examples.relationships.models.Category
import java.sql.ResultSet

class CategoryRepository(private val database: Database) {

    fun create(name: String, description: String? = null): Category {
        val categoryId = database.transaction {
            val query = insertInto(Categories) {
                it[Categories.name] = name
                description?.let { d -> it[Categories.description] = d }
            }
            insert(query)
        }

        return findById(categoryId)!!
    }

    fun findById(id: Long): Category? {
        return database.transaction {
            val query = from(Categories).where(Categories.id eq id)
            val results = select(query, ::mapCategory)
            results.firstOrNull()
        }
    }

    fun findAll(): List<Category> {
        return database.transaction {
            val query = from(Categories).orderBy(Categories.name)
            select(query, ::mapCategory)
        }
    }

    private fun mapCategory(rs: ResultSet) = Category(
        id = rs.getLong("id"),
        name = rs.getString("name"),
        description = rs.getString("description"),
        createdAt = rs.getLong("created_at")
    )
}
