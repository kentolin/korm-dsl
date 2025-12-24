package com.korm.dsl.dialect

interface Dialect {
    val name: String
    fun dataType(type: String): String
    fun autoIncrement(): String
    fun limit(count: Int, offset: Int = 0): String
    fun placeholder(index: Int): String
}