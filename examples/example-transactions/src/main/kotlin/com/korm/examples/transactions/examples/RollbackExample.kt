// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/RollbackExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal

class RollbackExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Rollback Example ===")
        println("Demonstrating automatic rollback on error\n")

        // Create test account
        val user = userService.createUser("bob", "bob@example.com")
        val account = accountService.createAccount(user.id, "ACC002", BigDecimal("500.00"))

        println("Initial balance: ${account.balance}\n")

        // Attempt to withdraw more than available
        println("Attempting to withdraw $1000 (more than balance)...")
        try {
            accountService.withdraw(account.id, BigDecimal("1000.00"))
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }

        // Check balance - should be unchanged
        val currentBalance = accountService.getBalance(account.id)
        println("\nBalance after failed withdrawal: $currentBalance")
        println("Transaction was rolled back automatically!")

        // Demonstrate explicit rollback
        println("\n--- Explicit Rollback Test ---")
        println("Starting transaction with multiple operations...")

        try {
            database.transaction {
                // First operation
                accountService.deposit(account.id, BigDecimal("100.00"))
                println("Deposited $100")

                // Second operation
                accountService.withdraw(account.id, BigDecimal("50.00"))
                println("Withdrew $50")

                // Force an error to trigger rollback
                throw RuntimeException("Simulated error - rolling back all operations")
            }
        } catch (e: Exception) {
            println("\nError: ${e.message}")
        }

        val finalBalance = accountService.getBalance(account.id)
        println("\nFinal balance: $finalBalance")
        println("Both deposit and withdrawal were rolled back!")
    }
}
