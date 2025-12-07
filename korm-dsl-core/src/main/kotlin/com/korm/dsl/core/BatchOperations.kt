// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/BatchOperations.kt

package com.korm.dsl.core

import com.korm.dsl.query.InsertQuery
import com.korm.dsl.query.UpdateQuery
import com.korm.dsl.schema.Table

/**
 * Batch insert operations.
 */
class BatchInsert(private val table: Table, private val batchSize: Int = 1000) {
    private val batches = mutableListOf<InsertQuery>()

    fun add(block: InsertQuery.() -> Unit): BatchInsert {
        batches.add(InsertQuery(table).apply(block))
        return this
    }

    fun execute(database: Database): IntArray {
        return database.transaction {
            val results = mutableListOf<Int>()

            batches.chunked(batchSize).forEach { chunk ->
                val sql = chunk.first().toSql()
                val statement = connection.prepareStatement(sql)

                chunk.forEach { query ->
                    query.bindParameters(statement)
                    statement.addBatch()
                }

                val batchResults = statement.executeBatch()
                results.addAll(batchResults.toList())
            }

            results.toIntArray()
        }
    }
}

/**
 * Batch update operations.
 */
class BatchUpdate(private val table: Table, private val batchSize: Int = 1000) {
    private val batches = mutableListOf<UpdateQuery>()

    fun add(block: UpdateQuery.() -> Unit): BatchUpdate {
        batches.add(UpdateQuery(table).apply(block))
        return this
    }

    fun execute(database: Database): IntArray {
        return database.transaction {
            val results = mutableListOf<Int>()

            batches.chunked(batchSize).forEach { chunk ->
                val sql = chunk.first().toSql()
                val statement = connection.prepareStatement(sql)

                chunk.forEach { query ->
                    query.bindParameters(statement)
                    statement.addBatch()
                }

                val batchResults = statement.executeBatch()
                results.addAll(batchResults.toList())
            }

            results.toIntArray()
        }
    }
}

/**
 * Create a batch insert operation.
 */
fun Database.batchInsert(table: Table, batchSize: Int = 1000): BatchInsert {
    return BatchInsert(table, batchSize)
}

/**
 * Create a batch update operation.
 */
fun Database.batchUpdate(table: Table, batchSize: Int = 1000): BatchUpdate {
    return BatchUpdate(table, batchSize)
}
