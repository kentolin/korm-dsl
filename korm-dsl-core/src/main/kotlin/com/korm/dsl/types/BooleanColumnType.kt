// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/BooleanColumnType.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Boolean column type.
 */
class BooleanColumnType : ColumnType<Boolean> {
    override fun sqlType() = "BOOLEAN"

    override fun valueToSql(value: Boolean) = value.toString().uppercase()

    override fun readFrom(rs: ResultSet, columnName: String): Boolean {
        return rs.getBoolean(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Boolean?) {
        if (value != null) {
            statement.setBoolean(index, value)
        } else {
            statement.setNull(index, java.sql.Types.BOOLEAN)
        }
    }
}
