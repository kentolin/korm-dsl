
/**
 * Migration metadata stored in database.
 */
data class MigrationHistory(
    val id: Long = 0,
    val version: Long,
    val description: String,
    val executedAt: Instant = Instant.now(),
    val executionTimeMs: Long,
    val success: Boolean,
    val errorMessage: String? = null,
    val checksum: String
)
