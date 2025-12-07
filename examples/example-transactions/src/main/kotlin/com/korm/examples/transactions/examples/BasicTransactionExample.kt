// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/BasicTransactionExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal

class BasicTransactionExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Basic Transaction Example ===")
        println("Demonstrating ACID properties and basic transactions\n")

        // Create user and account in a single transaction
        println("Creating user and account in one transaction...")
        val user = userService.createUser("alice", "alice@example.com")
        val account = accountService.createAccount(user.id, "ACC001", BigDecimal("1000.00"))

        println("User created: ${user.username} (ID: ${user.id})")
        println("Account created: ${account.accountNumber} with balance ${account.balance}\n")

        // Perform deposit
        println("Depositing $500...")
        val updatedAccount = accountService.deposit(account.id, BigDecimal("500.00"))
        println("New balance: ${updatedAccount.balance}\n")

        // Perform withdrawal
        println("Withdrawing $300...")
        val finalAccount = accountService.withdraw(account.id, BigDecimal("300.00"))
        println("Final balance: ${finalAccount.balance}\n")

        println("Transaction completed successfully!")
        println("All changes were committed atomically.")
    }
}
