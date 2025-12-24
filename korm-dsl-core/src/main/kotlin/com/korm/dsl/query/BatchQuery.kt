package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

/**
 * Batch insert operation for efficient bulk inserts
 */
class BatchInsertQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    private val batches = mutableListOf<Map<String, Any?>>()

    /**
     * Add a row to the batch
     */
    fun addBatch(values: Map<Column<*>, Any?>): BatchInsertQuery<T> {
        val row = values.mapKeys { it.key.name }
        batches.add(row)
        return this
    }

    /**
     * Convenience method to add a batch using DSL-style
     */
    fun addBatch(block: BatchRowBuilder.() -> Unit): BatchInsertQuery<T> {
        val builder = BatchRowBuilder()
        builder.block()
        batches.add(builder.values)
        return this
    }

    /**
     * Execute the batch insert
     * @param batchSize Number of rows to insert per batch (default 1000)
     * @return Total number of rows inserted
     */
    fun execute(batchSize: Int = 1000): Int {
        if (batches.isEmpty()) return 0

        var totalInserted = 0
        val columnNames = batches.first().keys.toList()
        val placeholders = columnNames.joinToString(", ") { "?" }
        val sql = "INSERT INTO ${table.tableName} (${columnNames.joinToString(", ")}) VALUES ($placeholders)"

        db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                batches.chunked(batchSize).forEach { chunk ->
                    chunk.forEach { row ->
                        columnNames.forEachIndexed { index, colName ->
                            stmt.setObject(index + 1, row[colName])
                        }
                        stmt.addBatch()
                    }

                    val results = stmt.executeBatch()
                    totalInserted += results.sum()
                    stmt.clearBatch()
                }
            }
        }

        return totalInserted
    }

    class BatchRowBuilder {
        internal val values = mutableMapOf<String, Any?>()

        fun <V : Any> set(column: Column<V>, value: V?) {
            values[column.name] = value
        }
    }
}

/**
 * Batch update operation
 */
class BatchUpdateQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    private val batches = mutableListOf<BatchUpdate>()

    data class BatchUpdate(
        val updates: Map<String, Any?>,
        val whereColumn: String,
        val whereValue: Any?
    )

    /**
     * Add an update to the batch
     */
    fun addBatch(
        updates: Map<Column<*>, Any?>,
        whereColumn: Column<*>,
        whereValue: Any?
    ): BatchUpdateQuery<T> {
        batches.add(
            BatchUpdate(
                updates = updates.mapKeys { it.key.name },
                whereColumn = whereColumn.name,
                whereValue = whereValue
            )
        )
        return this
    }

    /**
     * Execute the batch update
     * @param batchSize Number of updates per batch
     * @return Total number of rows updated
     */
    fun execute(batchSize: Int = 1000): Int {
        if (batches.isEmpty()) return 0

        var totalUpdated = 0
        val firstBatch = batches.first()
        val updateColumns = firstBatch.updates.keys.toList()
        val setClause = updateColumns.joinToString(", ") { "$it = ?" }
        val sql = "UPDATE ${table.tableName} SET $setClause WHERE ${firstBatch.whereColumn} = ?"

        db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                batches.chunked(batchSize).forEach { chunk ->
                    chunk.forEach { batch ->
                        var paramIndex = 1
                        updateColumns.forEach { colName ->
                            stmt.setObject(paramIndex++, batch.updates[colName])
                        }
                        stmt.setObject(paramIndex, batch.whereValue)
                        stmt.addBatch()
                    }

                    val results = stmt.executeBatch()
                    totalUpdated += results.sum()
                    stmt.clearBatch()
                }
            }
        }

        return totalUpdated
    }
}

/**
 * Extension functions for batch operations
 */
fun <T : Table> T.batchInsert(db: Database) = BatchInsertQuery(this, db)

fun <T : Table> T.batchUpdate(db: Database) = BatchUpdateQuery(this, db)