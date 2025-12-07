// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/VersionControl.kt

package com.korm.dsl.migrations

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Version control for migrations.
 */
object VersionControl {

    /**
     * Generate version number based on timestamp.
     * Format: YYYYMMDDHHmmss
     */
    fun generateVersion(): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        return LocalDateTime.now().format(formatter).toLong()
    }

    /**
     * Generate version with sequence number.
     * Format: YYYYMMDDHHmmssNN
     */
    fun generateVersionWithSequence(sequence: Int): Long {
        val base = generateVersion()
        return base * 100 + sequence
    }

    /**
     * Parse version to timestamp.
     */
    fun parseVersion(version: Long): LocalDateTime? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val versionStr = version.toString().take(14)
            LocalDateTime.parse(versionStr, formatter)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format version for display.
     */
    fun formatVersion(version: Long): String {
        val timestamp = parseVersion(version)
        return timestamp?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ?: version.toString()
    }

    /**
     * Validate version format.
     */
    fun isValidVersion(version: Long): Boolean {
        return parseVersion(version) != null
    }

    /**
     * Compare versions.
     */
    fun compare(v1: Long, v2: Long): Int {
        return v1.compareTo(v2)
    }
}
