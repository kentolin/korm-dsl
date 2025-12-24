package com.korm.dsl.schema

class PrimaryKey(
    val table: Table,
    val columns: List<Column<*>>
)