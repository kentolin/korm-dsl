package com.korm.dsl.core

import java.sql.Connection

class Transaction(private val connection: Connection) {

    fun <T> execute(block: () -> T): T {
        connection.autoCommit = false
        return try {
            val result = block()
            connection.commit()
            result
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = true
        }
    }
}

fun <T> Database.transaction(block: (Connection) -> T): T {
    return useConnection { conn ->
        Transaction(conn).execute { block(conn) }
    }
}