/**
 * Literal value expression.
 */
class LiteralExpression<T>(
    private val value: T,
    private val formatter: (T) -> String
) : Expression {
    override fun toSql(): String = formatter(value)
}
