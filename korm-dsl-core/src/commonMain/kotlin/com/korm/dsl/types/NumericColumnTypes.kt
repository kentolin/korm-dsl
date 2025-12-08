// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/NumericColumnTypes.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Float column type.
 */
class FloatColumnType : ColumnType<Float> {
    override fun sqlType() = "REAL"

    override fun valueToSql(value: Float) = value.toString()

    override fun readFrom(rs: ResultSet, columnName: String): Float {
        return rs.getFloat(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Float?) {
        if (value != null) {
            statement.setFloat(index, value)
        } else {
            statement.setNull(index, java.sql.Types.REAL)
        }
    }
}

/**
 * Big Decimal column type.
 */
class BigDecimalColumnType(
    private val precision: Int,
    private val scale: Int
) : ColumnType<java.math.BigDecimal> {
    override fun sqlType() = "DECIMAL($precision, $scale)"

    override fun valueToSql(value: java.math.BigDecimal) = value.toString()

    override fun readFrom(rs: ResultSet, columnName: String): java.math.BigDecimal {
        return rs.getBigDecimal(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: java.math.BigDecimal?) {
        if (value != null) {
            statement.setBigDecimal(index, value)
        } else {
            statement.setNull(index, java.sql.Types.DECIMAL)
        }
    }
}
