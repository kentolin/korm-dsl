// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/JsonColumnTypes.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * JSON column type (PostgreSQL).
 */
class JsonColumnType : ColumnType<String> {
    override fun sqlType() = "JSON"

    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"

    override fun readFrom(rs: ResultSet, columnName: String): String {
        return rs.getString(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        if (value != null) {
            val obj = org.postgresql.util.PGobject()
            obj.type = "json"
            obj.value = value
            statement.setObject(index, obj)
        } else {
            statement.setNull(index, java.sql.Types.OTHER)
        }
    }
}

/**
 * JSONB column type (PostgreSQL - binary JSON, faster).
 */
class JsonbColumnType : ColumnType<String> {
    override fun sqlType() = "JSONB"

    override fun valueToSql(value: String) = "'${value.replace("'", "''")}'"

    override fun readFrom(rs: ResultSet, columnName: String): String {
        return rs.getString(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: String?) {
        if (value != null) {
            val obj = org.postgresql.util.PGobject()
            obj.type = "jsonb"
            obj.value = value
            statement.setObject(index, obj)
        } else {
            statement.setNull(index, java.sql.Types.OTHER)
        }
    }
}
