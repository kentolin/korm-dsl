// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/SavepointExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal
import java.sql.Savepoint

class SavepointExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Savepoint Example ===")
        println("Demonstrating partial rollback using savepoints\n")

        // Create account
        val user = userService.createUser("jack", "jack@example.com")
        val account = accountService.createAccount(user.id, "ACC010", BigDecimal("1000.00"))

        println("Initial balance: ${account.balance}\n")

        database.transaction {
            println("Transaction started")
            val connection = getConnection()

            // Operation 1: Deposit $500
            accountService.deposit(account.id, BigDecimal("500.00"))
            println("  Deposited $500 - Balance: ${accountService.getBalance(account.id)}")

            // Create savepoint 1
            val savepoint1: Savepoint = connection.setSavepoint("savepoint1")
            println("  Created savepoint1")

            // Operation 2: Withdraw $200
            accountService.withdraw(account.id, BigDecimal("200.00"))
            println("  Withdrew $200 - Balance: ${accountService.getBalance(account.id)}")

            // Create savepoint 2
            val savepoint2: Savepoint = connection.setSavepoint("savepoint2")
            println("  Created savepoint2")

            // Operation 3: Withdraw $100
            accountService.withdraw(account.id, BigDecimal("100.00"))
            println("  Withdrew $100 - Balance: ${accountService.getBalance(account.id)}")

            // Rollback to savepoint2 (undo operation 3)
            println("\n  Rolling back to savepoint2...")
            connection.rollback(savepoint2)
            println("  Balance after rollback: ${accountService.getBalance(account.id)}")

            // Operation 4: Deposit $50
            accountService.deposit(account.id, BigDecimal("50.00"))
            println("  Deposited $50 - Balance: ${accountService.getBalance(account.id)}")

            println("\nCommitting transaction...")
        }

        val finalBalance = accountService.getBalance(account.id)
        println("\n--- Final Balance ---")
        println("Account balance: $finalBalance")
        println("\nExpected: 1000 + 500 - 200 + 50 = 1350")
        println("Operation 3 (withdraw $100) was rolled back using savepoint")
    }
}
