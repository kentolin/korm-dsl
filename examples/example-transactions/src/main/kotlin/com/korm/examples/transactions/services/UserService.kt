// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/services/UserService.kt

package com.korm.examples.transactions.services

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import java.sql.ResultSet

class UserService(private val database: Database) {

    fun createUser(username: String, email: String): User {
        val userId = database.transaction {
            val query = insertInto(Users) {
                it[Users.username] = username
                it[Users.email] = email
            }
            insert(query)
        }

        return findById(userId)!!
    }

    fun findById(id: Long): User? {
        return database.transaction {
            val query = from(Users).where(Users.id eq id)
            val results = select(query, ::mapUser)
            results.firstOrNull()
        }
    }

    private fun mapUser(rs: ResultSet): User {
        return User(
            id = rs.getLong("id"),
            username = rs.getString("username"),
            email = rs.getString("email"),
            createdAt = rs.getLong("created_at")
        )
    }
}
