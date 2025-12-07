// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/services/AccountService.kt

package com.korm.examples.transactions.services

import com.korm.dsl.core.Database
import com.korm.dsl.core.TransactionIsolation
import com.korm.dsl.core.TransactionConfig
import com.korm.examples.transactions.models.*
import java.math.BigDecimal
import java.sql.ResultSet

class AccountService(private val database: Database) {

    fun createAccount(userId: Long, accountNumber: String, initialBalance: BigDecimal): Account {
        val accountId = database.transaction {
            val query = insertInto(Accounts) {
                it[Accounts.userId] = userId
                it[Accounts.accountNumber] = accountNumber
                it[Accounts.balance] = initialBalance
            }
            insert(query)
        }

        return findById(accountId)!!
    }

    fun findById(id: Long): Account? {
        return database.transaction {
            val query = from(Accounts).where(Accounts.id eq id)
            val results = select(query, ::mapAccount)
            results.firstOrNull()
        }
    }

    fun findByAccountNumber(accountNumber: String): Account? {
        return database.transaction {
            val query = from(Accounts).where(Accounts.accountNumber eq accountNumber)
            val results = select(query, ::mapAccount)
            results.firstOrNull()
        }
    }

    fun getBalance(accountId: Long): BigDecimal {
        return findById(accountId)?.balance ?: BigDecimal.ZERO
    }

    fun deposit(accountId: Long, amount: BigDecimal): Account {
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Deposit amount must be positive")
        }

        database.transaction {
            val account = findById(accountId) ?: throw IllegalArgumentException("Account not found")
            val newBalance = account.balance.add(amount)

            val query = update(Accounts) {
                it[Accounts.balance] = newBalance
                it[Accounts.version] = account.version + 1
                it[Accounts.updatedAt] = System.currentTimeMillis()
            }.where(Accounts.id eq accountId)

            update(query)
        }

        return findById(accountId)!!
    }

    fun withdraw(accountId: Long, amount: BigDecimal): Account {
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Withdrawal amount must be positive")
        }

        database.transaction {
            val account = findById(accountId) ?: throw IllegalArgumentException("Account not found")

            if (account.balance < amount) {
                throw InsufficientFundsException("Insufficient funds: ${account.balance} < $amount")
            }

            val newBalance = account.balance.subtract(amount)

            val query = update(Accounts) {
                it[Accounts.balance] = newBalance
                it[Accounts.version] = account.version + 1
                it[Accounts.updatedAt] = System.currentTimeMillis()
            }.where(Accounts.id eq accountId)

            update(query)
        }

        return findById(accountId)!!
    }

    fun transfer(fromAccountId: Long, toAccountId: Long, amount: BigDecimal): TransferLog {
        if (amount <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Transfer amount must be positive")
        }

        if (fromAccountId == toAccountId) {
            throw IllegalArgumentException("Cannot transfer to same account")
        }

        var transferLogId: Long = 0
        var status = TransferStatus.PENDING
        var errorMessage: String? = null

        try {
            database.transaction {
                // Lock accounts in a consistent order to prevent deadlocks
                val (firstId, secondId) = if (fromAccountId < toAccountId) {
                    fromAccountId to toAccountId
                } else {
                    toAccountId to fromAccountId
                }

                // Create transfer log
                transferLogId = insertInto(TransferLogs) {
                    it[TransferLogs.fromAccountId] = fromAccountId
                    it[TransferLogs.toAccountId] = toAccountId
                    it[TransferLogs.amount] = amount
                    it[TransferLogs.status] = TransferStatus.PENDING.name
                }.let { insert(it) }

                // Lock both accounts
                val fromAccount = findByIdForUpdate(firstId)
                    ?: throw IllegalArgumentException("From account not found")
                val toAccount = findByIdForUpdate(secondId)
                    ?: throw IllegalArgumentException("To account not found")

                // Verify from account has sufficient funds
                if (fromAccount.balance < amount) {
                    throw InsufficientFundsException(
                        "Insufficient funds in account ${fromAccount.accountNumber}: ${fromAccount.balance} < $amount"
                    )
                }

                // Withdraw from source account
                update(Accounts) {
                    it[Accounts.balance] = fromAccount.balance.subtract(amount)
                    it[Accounts.version] = fromAccount.version + 1
                    it[Accounts.updatedAt] = System.currentTimeMillis()
                }.where(Accounts.id eq fromAccountId).let { update(it) }

                // Deposit to destination account
                update(Accounts) {
                    it[Accounts.balance] = toAccount.balance.add(amount)
                    it[Accounts.version] = toAccount.version + 1
                    it[Accounts.updatedAt] = System.currentTimeMillis()
                }.where(Accounts.id eq toAccountId).let { update(it) }

                // Update transfer log status
                status = TransferStatus.COMPLETED
                update(TransferLogs) {
                    it[TransferLogs.status] = status.name
                }.where(TransferLogs.id eq transferLogId).let { update(it) }
            }
        } catch (e: Exception) {
            status = TransferStatus.FAILED
            errorMessage = e.message

            // Update transfer log with error
            if (transferLogId > 0) {
                database.transaction {
                    update(TransferLogs) {
                        it[TransferLogs.status] = status.name
                        it[TransferLogs.errorMessage] = errorMessage
                    }.where(TransferLogs.id eq transferLogId).let { update(it) }
                }
            }

            throw e
        }

        return TransferLog(
            id = transferLogId,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            status = status,
            errorMessage = errorMessage
        )
    }

    /**
     * Find account by ID with SELECT FOR UPDATE lock.
     */
    private fun findByIdForUpdate(id: Long): Account? {
        return database.transaction {
            val sql = "SELECT * FROM accounts WHERE id = ? FOR UPDATE"
            var account: Account? = null

            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            account = mapAccount(rs)
                        }
                    }
                }
            }

            account
        }
    }

    private fun mapAccount(rs: ResultSet): Account {
        return Account(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            accountNumber = rs.getString("account_number"),
            balance = rs.getBigDecimal("balance"),
            currency = rs.getString("currency"),
            active = rs.getBoolean("active"),
            version = rs.getLong("version"),
            createdAt = rs.getLong("created_at"),
            updatedAt = rs.getLong("updated_at")
        )
    }
}

class InsufficientFundsException(message: String) : Exception(message)
