// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/mapping/ResultMapper.kt

package com.korm.dsl.mapping

import java.sql.ResultSet
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Maps database result sets to Kotlin objects.
 */
interface ResultMapper<T> {
    /**
     * Map a ResultSet row to an object.
     */
    fun map(rs: ResultSet): T
}

/**
 * Automatic result mapper using Kotlin reflection.
 */
class AutoResultMapper<T : Any>(private val kClass: KClass<T>) : ResultMapper<T> {

    override fun map(rs: ResultSet): T {
        val constructor = kClass.primaryConstructor
            ?: throw IllegalArgumentException("Class ${kClass.simpleName} must have a primary constructor")

        val params = constructor.parameters.associateWith { param ->
            getValueFromResultSet(rs, param)
        }

        return constructor.callBy(params)
    }

    private fun getValueFromResultSet(rs: ResultSet, param: KParameter): Any? {
        val columnName = param.name ?: throw IllegalArgumentException("Parameter name is required")

        return when (param.type.classifier) {
            String::class -> rs.getString(columnName)
            Int::class -> rs.getInt(columnName).takeIf { !rs.wasNull() }
            Long::class -> rs.getLong(columnName).takeIf { !rs.wasNull() }
            Boolean::class -> rs.getBoolean(columnName).takeIf { !rs.wasNull() }
            Double::class -> rs.getDouble(columnName).takeIf { !rs.wasNull() }
            Float::class -> rs.getFloat(columnName).takeIf { !rs.wasNull() }
            else -> {
                if (param.type.isMarkedNullable) {
                    rs.getObject(columnName)
                } else {
                    rs.getObject(columnName) ?: throw IllegalStateException("Column $columnName is null but parameter is not nullable")
                }
            }
        }
    }
}

/**
 * Builder for creating custom result mappers.
 */
class ResultMapperBuilder<T> {
    private val mappings = mutableMapOf<String, (ResultSet) -> Any?>()

    fun <V> map(columnName: String, mapper: (ResultSet) -> V) {
        mappings[columnName] = mapper
    }

    fun build(constructor: (Map<String, Any?>) -> T): ResultMapper<T> {
        return object : ResultMapper<T> {
            override fun map(rs: ResultSet): T {
                val values = mappings.mapValues { (_, mapper) -> mapper(rs) }
                return constructor(values)
            }
        }
    }
}

/**
 * Create an automatic result mapper for a class.
 */
inline fun <reified T : Any> autoMapper(): ResultMapper<T> {
    return AutoResultMapper(T::class)
}

/**
 * Create a custom result mapper using a builder.
 */
inline fun <T> resultMapper(block: ResultMapperBuilder<T>.() -> Unit): ResultMapperBuilder<T> {
    return ResultMapperBuilder<T>().apply(block)
}
