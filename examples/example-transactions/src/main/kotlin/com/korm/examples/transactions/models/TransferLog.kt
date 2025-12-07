// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/models/TransferLog.kt

package com.korm.examples.transactions.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.math.BigDecimal

data class TransferLog(
    val id: Long = 0,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal,
    val status: TransferStatus,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransferStatus {
    PENDING,
    COMPLETED,
    FAILED,
    ROLLED_BACK
}

object TransferLogs : Table("transfer_logs") {
    val id = long("id").autoIncrement()
    val fromAccountId = long("from_account_id").notNull()
    val toAccountId = long("to_account_id").notNull()
    val amount = decimal("amount", 15, 2).notNull()
    val status = varchar("status", 20).notNull()
    val errorMessage = text("error_message")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(fromAccountId, Accounts, Accounts.id)
        foreignKey(toAccountId, Accounts, Accounts.id)
    }
}
