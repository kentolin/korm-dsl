// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/Pagination.kt

package com.korm.dsl.query

/**
 * Pagination result.
 */
data class Page<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int
) {
    val isFirst: Boolean get() = pageNumber == 0
    val isLast: Boolean get() = pageNumber == totalPages - 1
    val hasNext: Boolean get() = pageNumber < totalPages - 1
    val hasPrevious: Boolean get() = pageNumber > 0
}

/**
 * Pageable request.
 */
data class Pageable(
    val pageNumber: Int = 0,
    val pageSize: Int = 20,
    val sort: Sort? = null
)

/**
 * Sort specification.
 */
data class Sort(
    val orders: List<Order>
) {
    data class Order(
        val property: String,
        val direction: Direction = Direction.ASC
    )

    enum class Direction {
        ASC, DESC
    }

    companion object {
        fun by(vararg orders: Order): Sort = Sort(orders.toList())
        fun by(property: String, direction: Direction = Direction.ASC): Sort =
            Sort(listOf(Order(property, direction)))
    }
}

/**
 * Extension to apply pagination to SelectQuery.
 */
fun SelectQuery.paginate(pageable: Pageable): SelectQuery {
    val offset = pageable.pageNumber * pageable.pageSize
    this.limit(pageable.pageSize).offset(offset)

    pageable.sort?.orders?.forEach { order ->
        this.orderBy(order.property, order.direction.name)
    }

    return this
}
