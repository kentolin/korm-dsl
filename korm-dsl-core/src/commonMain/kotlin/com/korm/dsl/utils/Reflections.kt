// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/utils/Reflections.kt

package com.korm.dsl.utils

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Reflection utilities for KORM.
 */
object Reflections {

    /**
     * Get all properties of a class.
     */
    fun <T : Any> getProperties(kClass: KClass<T>): Collection<KProperty1<T, *>> {
        return kClass.memberProperties
    }

    /**
     * Get primary constructor parameters.
     */
    fun <T : Any> getConstructorParameters(kClass: KClass<T>): List<String> {
        return kClass.primaryConstructor?.parameters?.mapNotNull { it.name } ?: emptyList()
    }

    /**
     * Check if a class has a no-arg constructor.
     */
    fun <T : Any> hasNoArgConstructor(kClass: KClass<T>): Boolean {
        return kClass.constructors.any { it.parameters.isEmpty() }
    }

    /**
     * Get property by name.
     */
    fun <T : Any> getProperty(kClass: KClass<T>, name: String): KProperty1<T, *>? {
        return kClass.memberProperties.find { it.name == name }
    }

    /**
     * Create an instance using the primary constructor.
     */
    fun <T : Any> createInstance(kClass: KClass<T>, params: Map<String, Any?>): T {
        val constructor = kClass.primaryConstructor
            ?: throw IllegalArgumentException("Class ${kClass.simpleName} must have a primary constructor")

        val args = constructor.parameters.associateWith { param ->
            params[param.name] ?: if (param.isOptional) null
            else throw IllegalArgumentException("Missing required parameter: ${param.name}")
        }

        return constructor.callBy(args)
    }
}
