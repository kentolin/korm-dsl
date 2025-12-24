package com.korm.dsl.schema

class ForeignKey(
    val fromTable: Table,
    val fromColumn: Column<*>,
    val toTable: Table,
    val toColumn: Column<*>
)