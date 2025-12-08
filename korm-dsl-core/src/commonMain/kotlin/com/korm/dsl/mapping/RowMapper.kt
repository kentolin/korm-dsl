// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/mapping/RowMapper.kt

package com.korm.dsl.mapping

import com.korm.dsl.schema.Column
import java.sql.ResultSet

/**
 * Simple row mapper for columns.
 */
class RowMapper {

    /**
     * Map a single column value.
     */
    fun <T> mapColumn(rs: ResultSet, column: Column<T>): T {
        return column.type.readFrom(rs, column.name)
    }

    /**
     * Map multiple columns.
     */
    fun mapColumns(rs: ResultSet, columns: List<Column<*>>): Map<String, Any?> {
        return columns.associate { column ->
            column.name to column.type.readFrom(rs, column.name)
        }
    }

    /**
     * Check if a column is null.
     */
    fun isNull(rs: ResultSet, column: Column<*>): Boolean {
        rs.getObject(column.name)
        return rs.wasNull()
    }
}
