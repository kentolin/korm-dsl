package com.korm.dsl.migrations

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

/**
 * Temporary table for creating columns in migrations
 * This is used internally when we need to create columns without a specific table context
 */
private object TempTable : Table("_temp")

/**
 * Create an INT column for use in migrations
 */
fun int(name: String): Column<Int> {
    return Column(name, "INT", TempTable)
}

/**
 * Create a BIGINT column for use in migrations
 */
fun long(name: String): Column<Long> {
    return Column(name, "BIGINT", TempTable)
}

/**
 * Create a VARCHAR column for use in migrations
 */
fun varchar(name: String, length: Int = 255): Column<String> {
    return Column(name, "VARCHAR($length)", TempTable)
}

/**
 * Create a TEXT column for use in migrations
 */
fun text(name: String): Column<String> {
    return Column(name, "TEXT", TempTable)
}

/**
 * Create a BOOLEAN column for use in migrations
 */
fun bool(name: String): Column<Boolean> {
    return Column(name, "BOOLEAN", TempTable)
}

/**
 * Create a DOUBLE column for use in migrations
 */
fun double(name: String): Column<Double> {
    return Column(name, "DOUBLE", TempTable)
}
