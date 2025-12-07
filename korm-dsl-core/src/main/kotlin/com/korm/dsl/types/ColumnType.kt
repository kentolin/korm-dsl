// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/ColumnType.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Base interface for column type definitions.
 */
interface ColumnType<T> {
    /**
     * Get SQL type definition.
     */
    fun sqlType(): String

    /**
     * Convert value to SQL string representation.
     */
    fun valueToSql(value: T): String

    /**
     * Read value from ResultSet.
     */
    fun readFrom(rs: ResultSet, columnName: String): T

    /**
     * Bind value to PreparedStatement.
     */
    fun bindTo(statement: PreparedStatement, index: Int, value: T?)
}

class IntColumnType : ColumnType<Int> {
    override fun sqlType() = "INTEGER"
    override fun valueToSql(value: Int) = value.toString()
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getInt(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: Int?) {
        if (value != null) statement.setInt(index, value)
        else statement.setNull(index, java.sql.Types.INTEGER)
    }
}

class LongColumnType : ColumnType<Long> {
    override fun sqlType() = "BIGINT"
    override fun valueToSql(value: Long) = value.toString()
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getLong(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: Long?) {
        if (value != null) statement.setLong(index, value)
        else statement.setNull(index, java.sql.Types.BIGINT)
    }
}

class VarcharColumnType(private val length: Int) : ColumnType<String> {
    override fun sqlType() = "VARCHAR($length)"
    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getString(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        statement.setString(index, value)
    }
}

class TextColumnType : ColumnType<String> {
    override fun sqlType() = "TEXT"
    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getString(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        statement.setString(index, value)
    }
}

class BooleanColumnType : ColumnType<Boolean> {
    override fun sqlType() = "BOOLEAN"
    override fun valueToSql(value: Boolean) = value.toString()
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getBoolean(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: Boolean?) {
        if (value != null) statement.setBoolean(index, value)
        else statement.setNull(index, java.sql.Types.BOOLEAN)
    }
}

class TimestampColumnType : ColumnType<Long> {
    override fun sqlType() = "BIGINT"
    override fun valueToSql(value: Long) = value.toString()
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getLong(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: Long?) {
        if (value != null) statement.setLong(index, value)
        else statement.setNull(index, java.sql.Types.BIGINT)
    }
}

class DecimalColumnType(
    private val precision: Int,
    private val scale: Int
) : ColumnType<Double> {
    override fun sqlType() = "DECIMAL($precision, $scale)"
    override fun valueToSql(value: Double) = value.toString()
    override fun readFrom(rs: ResultSet, columnName: String) = rs.getDouble(columnName)
    override fun bindTo(statement: PreparedStatement, index: Int, value: Double?) {
        if (value != null) statement.setDouble(index, value)
        else statement.setNull(index, java.sql.Types.DECIMAL)
    }
}
