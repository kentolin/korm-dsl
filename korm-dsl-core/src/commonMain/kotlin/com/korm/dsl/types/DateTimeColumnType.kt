// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/types/DateTimeColumnType.kt

package com.korm.dsl.types

import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * LocalDateTime column type.
 */
class LocalDateTimeColumnType : ColumnType<LocalDateTime> {
    override fun sqlType() = "TIMESTAMP"

    override fun valueToSql(value: LocalDateTime): String {
        return "'${Timestamp.valueOf(value)}'"
    }

    override fun readFrom(rs: ResultSet, columnName: String): LocalDateTime {
        return rs.getTimestamp(columnName).toLocalDateTime()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: LocalDateTime?) {
        if (value != null) {
            statement.setTimestamp(index, Timestamp.valueOf(value))
        } else {
            statement.setNull(index, java.sql.Types.TIMESTAMP)
        }
    }
}

/**
 * LocalDate column type.
 */
class LocalDateColumnType : ColumnType<LocalDate> {
    override fun sqlType() = "DATE"

    override fun valueToSql(value: LocalDate): String {
        return "'${java.sql.Date.valueOf(value)}'"
    }

    override fun readFrom(rs: ResultSet, columnName: String): LocalDate {
        return rs.getDate(columnName).toLocalDate()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: LocalDate?) {
        if (value != null) {
            statement.setDate(index, java.sql.Date.valueOf(value))
        } else {
            statement.setNull(index, java.sql.Types.DATE)
        }
    }
}

/**
 * LocalTime column type.
 */
class LocalTimeColumnType : ColumnType<LocalTime> {
    override fun sqlType() = "TIME"

    override fun valueToSql(value: LocalTime): String {
        return "'${java.sql.Time.valueOf(value)}'"
    }

    override fun readFrom(rs: ResultSet, columnName: String): LocalTime {
        return rs.getTime(columnName).toLocalTime()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: LocalTime?) {
        if (value != null) {
            statement.setTime(index, java.sql.Time.valueOf(value))
        } else {
            statement.setNull(index, java.sql.Types.TIME)
        }
    }
}

/**
 * Instant column type (stored as timestamp).
 */
class InstantColumnType : ColumnType<Instant> {
    override fun sqlType() = "TIMESTAMP"

    override fun valueToSql(value: Instant): String {
        return "'${Timestamp.from(value)}'"
    }

    override fun readFrom(rs: ResultSet, columnName: String): Instant {
        return rs.getTimestamp(columnName).toInstant()
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: Instant?) {
        if (value != null) {
            statement.setTimestamp(index, Timestamp.from(value))
        } else {
            statement.setNull(index, java.sql.Types.TIMESTAMP)
        }
    }
}

/**
 * UUID column type.
 */
class UUIDColumnType : ColumnType<java.util.UUID> {
    override fun sqlType() = "UUID"

    override fun valueToSql(value: java.util.UUID): String {
        return "'$value'"
    }

    override fun readFrom(rs: ResultSet, columnName: String): java.util.UUID {
        return java.util.UUID.fromString(rs.getString(columnName))
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: java.util.UUID?) {
        if (value != null) {
            statement.setObject(index, value)
        } else {
            statement.setNull(index, java.sql.Types.OTHER)
        }
    }
}

/**
 * ByteArray column type (BLOB).
 */
class ByteArrayColumnType : ColumnType<ByteArray> {
    override fun sqlType() = "BYTEA"

    override fun valueToSql(value: ByteArray): String {
        // This is database-specific
        return "?"
    }

    override fun readFrom(rs: ResultSet, columnName: String): ByteArray {
        return rs.getBytes(columnName)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: ByteArray?) {
        if (value != null) {
            statement.setBytes(index, value)
        } else {
            statement.setNull(index, java.sql.Types.BINARY)
        }
    }
}

/**
 * Enum column type.
 */
class EnumColumnType<E : Enum<E>>(private val enumClass: Class<E>) : ColumnType<E> {
    override fun sqlType() = "VARCHAR(255)"

    override fun valueToSql(value: E): String {
        return "'${value.name}'"
    }

    override fun readFrom(rs: ResultSet, columnName: String): E {
        val name = rs.getString(columnName)
        return java.lang.Enum.valueOf(enumClass, name)
    }

    override fun bindTo(statement: PreparedStatement, index: Int, value: E?) {
        if (value != null) {
            statement.setString(index, value.name)
        } else {
            statement.setNull(index, java.sql.Types.VARCHAR)
        }
    }
}

// Extension functions for new column types

fun Table.localDateTime(name: String): Column<LocalDateTime> {
    return Column(name, LocalDateTimeColumnType())
}

fun Table.localDate(name: String): Column<LocalDate> {
    return Column(name, LocalDateColumnType())
}

fun Table.localTime(name: String): Column<LocalTime> {
    return Column(name, LocalTimeColumnType())
}

fun Table.instant(name: String): Column<Instant> {
    return Column(name, InstantColumnType())
}

fun Table.uuid(name: String): Column<java.util.UUID> {
    return Column(name, UUIDColumnType())
}

fun Table.blob(name: String): Column<ByteArray> {
    return Column(name, ByteArrayColumnType())
}

inline fun <reified E : Enum<E>> Table.enum(name: String): Column<E> {
    return Column(name, EnumColumnType(E::class.java))
}
