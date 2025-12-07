// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/models/User.kt

package com.korm.examples.transactions.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table

data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 50).unique().notNull()
    val email = varchar("email", 255).unique().notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)
}
