// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/schema/TableExtensions.kt

package com.korm.dsl.schema

import com.korm.dsl.types.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Extension functions for Table to define columns with various types.
 */

// Integer types
fun Table.smallInt(name: String): Column<Short> = Column(name, SmallIntColumnType())
fun Table.tinyInt(name: String): Column<Byte> = Column(name, TinyIntColumnType())

// String types
fun Table.char(name: String, length: Int): Column<String> = Column(name, CharColumnType(length))
fun Table.clob(name: String): Column<String> = Column(name, ClobColumnType())

// Numeric types
fun Table.float(name: String): Column<Float> = Column(name, FloatColumnType())
fun Table.real(name: String): Column<Float> = Column(name, FloatColumnType())
fun Table.numeric(name: String, precision: Int, scale: Int): Column<Double> = Column(name, DecimalColumnType(precision, scale))
fun Table.money(name: String): Column<Double> = Column(name, DecimalColumnType(19, 4))

// Date/Time types (extensions)
fun Table.date(name: String): Column<LocalDate> = Column(name, LocalDateColumnType())
fun Table.time(name: String): Column<LocalTime> = Column(name, LocalTimeColumnType())
fun Table.datetime(name: String): Column<LocalDateTime> = Column(name, LocalDateTimeColumnType())

// JSON types (for PostgreSQL)
fun Table.json(name: String): Column<String> = Column(name, JsonColumnType())
fun Table.jsonb(name: String): Column<String> = Column(name, JsonbColumnType())

// Array types (for PostgreSQL)
fun Table.intArray(name: String): Column<IntArray> = Column(name, IntArrayColumnType())
fun Table.stringArray(name: String): Column<Array<String>> = Column(name, StringArrayColumnType())
