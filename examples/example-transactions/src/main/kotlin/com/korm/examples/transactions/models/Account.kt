// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/models/Account.kt

package com.korm.examples.transactions.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.math.BigDecimal

data class Account(
    val id: Long = 0,
    val userId: Long,
    val accountNumber: String,
    val balance: BigDecimal,
    val currency: String = "USD",
    val active: Boolean = true,
    val version: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object Accounts : Table("accounts") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").notNull()
    val accountNumber = varchar("account_number", 20).unique().notNull()
    val balance = decimal("balance", 15, 2).notNull()
    val currency = varchar("currency", 3).default("USD").notNull()
    val active = boolean("active").default(true).notNull()
    val version = long("version").default(0).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(userId, Users, Users.id)
        index("idx_account_number", listOf("account_number"), unique = true)
    }
}
