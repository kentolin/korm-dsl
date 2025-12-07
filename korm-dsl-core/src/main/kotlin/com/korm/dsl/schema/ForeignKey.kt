// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/schema/ForeignKey.kt

package com.korm.dsl.schema

/**
 * Represents a foreign key constraint.
 */
class ForeignKey(
    val column: Column<*>,
    val referencedTable: Table,
    val referencedColumn: Column<*>,
    val onDelete: ReferentialAction = ReferentialAction.NO_ACTION,
    val onUpdate: ReferentialAction = ReferentialAction.NO_ACTION
) {

    /**
     * Generate SQL definition for foreign key.
     */
    fun toSql(): String {
        val fkName = "fk_${column.name}_${referencedTable.tableName}_${referencedColumn.name}"
        return buildString {
            append("CONSTRAINT $fkName ")
            append("FOREIGN KEY (${column.name}) ")
            append("REFERENCES ${referencedTable.tableName}(${referencedColumn.name})")
            if (onDelete != ReferentialAction.NO_ACTION) {
                append(" ON DELETE ${onDelete.sql}")
            }
            if (onUpdate != ReferentialAction.NO_ACTION) {
                append(" ON UPDATE ${onUpdate.sql}")
            }
        }
    }
}

enum class ReferentialAction(val sql: String) {
    NO_ACTION("NO ACTION"),
    RESTRICT("RESTRICT"),
    CASCADE("CASCADE"),
    SET_NULL("SET NULL"),
    SET_DEFAULT("SET DEFAULT")
}
