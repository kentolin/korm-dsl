// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/mapping/EntityMapper.kt

package com.korm.dsl.mapping

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Maps entities to database tables and vice versa.
 */
class EntityMapper<T : Any>(
    private val kClass: KClass<T>,
    private val table: Table
) {
    private val propertyToColumn = mutableMapOf<KProperty1<T, *>, Column<*>>()

    /**
     * Map a property to a column.
     */
    fun <V> map(property: KProperty1<T, V>, column: Column<V>): EntityMapper<T> {
        propertyToColumn[property] = column
        return this
    }

    /**
     * Get column for a property.
     */
    fun getColumn(property: KProperty1<T, *>): Column<*>? {
        return propertyToColumn[property]
    }

    /**
     * Get all mapped columns.
     */
    fun getColumns(): List<Column<*>> {
        return propertyToColumn.values.toList()
    }

    /**
     * Extract values from an entity for insertion/update.
     */
    fun extractValues(entity: T): Map<Column<*>, Any?> {
        return propertyToColumn.mapValues { (property, _) ->
            property.get(entity)
        }
    }
}

/**
 * Builder for entity mappers.
 */
class EntityMapperBuilder<T : Any>(
    private val kClass: KClass<T>,
    private val table: Table
) {
    private val mapper = EntityMapper(kClass, table)

    fun <V> property(property: KProperty1<T, V>, column: Column<V>) {
        mapper.map(property, column)
    }

    fun build(): EntityMapper<T> = mapper
}

/**
 * Create an entity mapper.
 */
inline fun <reified T : Any> entityMapper(
    table: Table,
    block: EntityMapperBuilder<T>.() -> Unit
): EntityMapper<T> {
    return EntityMapperBuilder(T::class, table).apply(block).build()
}
