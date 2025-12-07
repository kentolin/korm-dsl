/**
 * Column reference expression.
 */
class ColumnExpression<T>(private val column: Column<T>) : Expression {
    override fun toSql(): String = column.name
}
