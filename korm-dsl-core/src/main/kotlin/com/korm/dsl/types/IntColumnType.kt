// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/IntColumnType.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Integer column type.
 */
class IntColumnType : ColumnType<Int> {
    override fun sqlType() = "INTEGER"

    override fun valueToSql(value: Int) = value.toString()

    override fun readFrom(rs: ResultSet, columnName: String): Int {
        return rs.getInt(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Int?) {
        if (value != null) {
            statement.setInt(index, value)
        } else {
            statement.setNull(index, java.sql.Types.INTEGER)
        }
    }
}

/**
 * Small integer column type.
 */
class SmallIntColumnType : ColumnType<Short> {
    override fun sqlType() = "SMALLINT"

    override fun valueToSql(value: Short) = value.toString()

    override fun readFrom(rs: ResultSet, columnName: String): Short {
        return rs.getShort(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Short?) {
        if (value != null) {
            statement.setShort(index, value)
        } else {
            statement.setNull(index, java.sql.Types.SMALLINT)
        }
    }
}

/**
 * Tiny integer column type (for databases that support it).
 */
class TinyIntColumnType : ColumnType<Byte> {
    override fun sqlType() = "TINYINT"

    override fun valueToSql(value: Byte) = value.toString()

    override fun readFrom(rs: ResultSet, columnName: String): Byte {
        return rs.getByte(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Byte?) {
        if (value != null) {
            statement.setByte(index, value)
        } else {
            statement.setNull(index, java.sql.Types.TINYINT)
        }
    }
}
