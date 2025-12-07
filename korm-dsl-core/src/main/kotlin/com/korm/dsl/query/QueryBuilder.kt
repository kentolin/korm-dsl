// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/QueryBuilder.kt

package com.korm.dsl.query

import com.korm.dsl.schema.Table

/**
 * Main query builder entry point.
 */
object QueryBuilder {

    /**
     * Start building a SELECT query.
     */
    fun select(table: Table): SelectQuery {
        return SelectQuery(table)
    }

    /**
     * Start building an INSERT query.
     */
    fun insertInto(table: Table, block: InsertQuery.() -> Unit): InsertQuery {
        return InsertQuery(table).apply(block)
    }

    /**
     * Start building an UPDATE query.
     */
    fun update(table: Table, block: UpdateQuery.() -> Unit): UpdateQuery {
        return UpdateQuery(table).apply(block)
    }

    /**
     * Start building a DELETE query.
     */
    fun deleteFrom(table: Table, block: DeleteQuery.() -> Unit): DeleteQuery {
        return DeleteQuery(table).apply(block)
    }
}
