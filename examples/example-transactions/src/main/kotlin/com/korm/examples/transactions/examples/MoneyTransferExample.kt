// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/MoneyTransferExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal

class MoneyTransferExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Money Transfer Example ===")
        println("Demonstrating safe money transfers with proper locking\n")

        // Create test accounts
        val user1 = userService.createUser("frank", "frank@example.com")
        val user2 = userService.createUser("grace", "grace@example.com")

        val account1 = accountService.createAccount(user1.id, "ACC006", BigDecimal("2000.00"))
        val account2 = accountService.createAccount(user2.id, "ACC007", BigDecimal("1000.00"))

        println("Initial balances:")
        println("  Account ${account1.accountNumber}: ${account1.balance}")
        println("  Account ${account2.accountNumber}: ${account2.balance}\n")

        // Successful transfer
        println("--- Transfer 1: $500 from ACC006 to ACC007 ---")
        val transfer1 = accountService.transfer(account1.id, account2.id, BigDecimal("500.00"))
        println("Transfer status: ${transfer1.status}")

        val balance1After = accountService.getBalance(account1.id)
        val balance2After = accountService.getBalance(account2.id)
        println("  Account ${account1.accountNumber}: $balance1After")
        println("  Account ${account2.accountNumber}: $balance2After\n")

        // Failed transfer (insufficient funds)
        println("--- Transfer 2: $3000 from ACC006 to ACC007 (will fail) ---")
        try {
            accountService.transfer(account1.id, account2.id, BigDecimal("3000.00"))
        } catch (e: Exception) {
            println("Transfer failed: ${e.message}")
        }

        val balance1Final = accountService.getBalance(account1.id)
        val balance2Final = accountService.getBalance(account2.id)
        println("  Account ${account1.accountNumber}: $balance1Final")
        println("  Account ${account2.accountNumber}: $balance2Final")
        println("  Balances unchanged after failed transfer\n")

        // Transfer in reverse direction
        println("--- Transfer 3: $300 from ACC007 to ACC006 ---")
        val transfer3 = accountService.transfer(account2.id, account1.id, BigDecimal("300.00"))
        println("Transfer status: ${transfer3.status}")

        val balance1End = accountService.getBalance(account1.id)
        val balance2End = accountService.getBalance(account2.id)
        println("  Account ${account1.accountNumber}: $balance1End")
        println("  Account ${account2.accountNumber}: $balance2End\n")

        println("All transfers completed with proper ACID guarantees!")
    }
}
