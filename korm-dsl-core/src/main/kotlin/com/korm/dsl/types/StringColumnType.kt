// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/StringColumnType.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * VARCHAR column type.
 */
class VarcharColumnType(private val length: Int) : ColumnType<String> {
    override fun sqlType() = "VARCHAR($length)"

    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"

    override fun readFrom(rs: ResultSet, columnName: String): String {
        return rs.getString(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        statement.setString(index, value)
    }
}

/**
 * CHAR column type (fixed length).
 */
class CharColumnType(private val length: Int) : ColumnType<String> {
    override fun sqlType() = "CHAR($length)"

    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"

    override fun readFrom(rs: ResultSet, columnName: String): String {
        return rs.getString(columnName).trim()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        statement.setString(index, value)
    }
}

/**
 * TEXT column type.
 */
class TextColumnType : ColumnType<String> {
    override fun sqlType() = "TEXT"

    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"

    override fun readFrom(rs: ResultSet, columnName: String): String {
        return rs.getString(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        statement.setString(index, value)
    }
}

/**
 * CLOB (Character Large Object) column type.
 */
class ClobColumnType : ColumnType<String> {
    override fun sqlType() = "CLOB"

    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"

    override fun readFrom(rs: ResultSet, columnName: String): String {
        val clob = rs.getClob(columnName)
        return clob?.getSubString(1, clob.length().toInt()) ?: ""
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        if (value != null) {
            statement.setString(index, value)
        } else {
            statement.setNull(index, java.sql.Types.CLOB)
        }
    }
}
