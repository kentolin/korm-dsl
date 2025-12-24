package com.korm.dsl.schema

class Index(
    val name: String,
    val table: Table,
    val columns: List<Column<*>>,
    val unique: Boolean = false
)