// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/ArrayColumnTypes.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Integer array column type (PostgreSQL).
 */
class IntArrayColumnType : ColumnType<IntArray> {
    override fun sqlType() = "INTEGER[]"

    override fun valueToSql(value: IntArray): String {
        return "ARRAY[${value.joinToString(",")}]"
    }

    override fun readFrom(rs: ResultSet, columnName: String): IntArray {
        val array = rs.getArray(columnName)
        return (array.array as Array<*>).map { (it as Number).toInt() }.toIntArray()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: IntArray?) {
        if (value != null) {
            val connection = statement.connection
            val sqlArray = connection.createArrayOf("INTEGER", value.toTypedArray())
            statement.setArray(index, sqlArray)
        } else {
            statement.setNull(index, java.sql.Types.ARRAY)
        }
    }
}

/**
 * String array column type (PostgreSQL).
 */
class StringArrayColumnType : ColumnType<Array<String>> {
    override fun sqlType() = "TEXT[]"

    override fun valueToSql(value: Array<String>): String {
        val escaped = value.joinToString(",") { "'${it.replace("'", "''")}'" }
        return "ARRAY[$escaped]"
    }

    override fun readFrom(rs: ResultSet, columnName: String): Array<String> {
        val array = rs.getArray(columnName)
        @Suppress("UNCHECKED_CAST")
        return array.array as Array<String>
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Array<String>?) {
        if (value != null) {
            val connection = statement.connection
            val sqlArray = connection.createArrayOf("TEXT", value)
            statement.setArray(index, sqlArray)
        } else {
            statement.setNull(index, java.sql.Types.ARRAY)
        }
    }
}

/**
 * Long array column type (PostgreSQL).
 */
class LongArrayColumnType : ColumnType<LongArray> {
    override fun sqlType() = "BIGINT[]"

    override fun valueToSql(value: LongArray): String {
        return "ARRAY[${value.joinToString(",")}]"
    }

    override fun readFrom(rs: ResultSet, columnName: String): LongArray {
        val array = rs.getArray(columnName)
        return (array.array as Array<*>).map { (it as Number).toLong() }.toLongArray()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: LongArray?) {
        if (value != null) {
            val connection = statement.connection
            val sqlArray = connection.createArrayOf("BIGINT", value.toTypedArray())
            statement.setArray(index, sqlArray)
        } else {
            statement.setNull(index, java.sql.Types.ARRAY)
        }
    }
}
