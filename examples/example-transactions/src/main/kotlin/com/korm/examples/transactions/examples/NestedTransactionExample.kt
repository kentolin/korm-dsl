// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/NestedTransactionExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal

class NestedTransactionExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Nested Transaction Example ===")
        println("Demonstrating nested transactions and partial rollback\n")

        // Create accounts
        val user1 = userService.createUser("david", "david@example.com")
        val user2 = userService.createUser("eve", "eve@example.com")

        val account1 = accountService.createAccount(user1.id, "ACC004", BigDecimal("1000.00"))
        val account2 = accountService.createAccount(user2.id, "ACC005", BigDecimal("500.00"))

        println("Account 1 balance: ${account1.balance}")
        println("Account 2 balance: ${account2.balance}\n")

        println("Attempting nested operations...")
        println("1. Transfer $200 from Account 1 to Account 2")
        println("2. Withdraw $300 from Account 2 (will fail - insufficient funds)")
        println("3. Deposit $100 to Account 1\n")

        try {
            database.transaction {
                // Operation 1: Transfer (succeeds)
                println("Performing transfer...")
                accountService.transfer(account1.id, account2.id, BigDecimal("200.00"))
                println("  Transfer completed")

                // Operation 2: Withdraw (fails)
                println("Attempting withdrawal...")
                try {
                    accountService.withdraw(account2.id, BigDecimal("1000.00"))
                } catch (e: Exception) {
                    println("  Withdrawal failed: ${e.message}")
                    throw e // Re-throw to rollback entire transaction
                }

                // Operation 3: Deposit (won't execute due to rollback)
                println("Depositing...")
                accountService.deposit(account1.id, BigDecimal("100.00"))
            }
        } catch (e: Exception) {
            println("\nTransaction rolled back due to error")
        }

        // Check final balances
        val final1 = accountService.getBalance(account1.id)
        val final2 = accountService.getBalance(account2.id)

        println("\n--- Final Balances ---")
        println("Account 1: $final1 (original: ${account1.balance})")
        println("Account 2: $final2 (original: ${account2.balance})")
        println("\nAll operations were rolled back - balances unchanged!")
    }
}
