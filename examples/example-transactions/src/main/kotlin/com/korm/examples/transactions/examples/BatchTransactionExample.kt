// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/BatchTransactionExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal

class BatchTransactionExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Batch Transaction Example ===")
        println("Demonstrating batch operations in a single transaction\n")

        // Create accounts
        val users = (1..5).map { i ->
            userService.createUser("user$i", "user$i@example.com")
        }

        println("Creating 5 accounts in a batch transaction...")

        val startTime = System.currentTimeMillis()

        val accounts = database.transaction {
            users.mapIndexed { index, user ->
                val accountNumber = "BATCH${(index + 1).toString().padStart(3, '0')}"
                val initialBalance = BigDecimal("${(index + 1) * 1000}.00")

                val accountId = insertInto(Accounts) {
                    it[Accounts.userId] = user.id
                    it[Accounts.accountNumber] = accountNumber
                    it[Accounts.balance] = initialBalance
                }.let { insert(it) }

                accountService.findById(accountId)!!
            }
        }

        val endTime = System.currentTimeMillis()

        println("Created ${accounts.size} accounts in ${endTime - startTime}ms\n")

        println("--- Created Accounts ---")
        accounts.forEach { account ->
            println("  ${account.accountNumber}: ${account.balance}")
        }

        // Batch update
        println("\n Performing batch updates...")

        database.transaction {
            accounts.forEach { account ->
                update(Accounts) {
                    it[Accounts.balance] = account.balance.multiply(BigDecimal("1.10"))
                    it[Accounts.updatedAt] = System.currentTimeMillis()
                }.where(Accounts.id eq account.id).let { update(it) }
            }
        }

        println("\n--- Updated Balances (10% increase) ---")
        accounts.forEach { account ->
            val updated = accountService.findById(account.id)!!
            println("  ${updated.accountNumber}: ${updated.balance}")
        }

        println("\nAll batch operations completed atomically!")
    }
}
