# Advanced Queries

Complete guide to advanced query features in KORM including Window Functions, CTEs, CASE expressions, and more.

---

## Overview

KORM provides powerful advanced query features for complex data analysis and transformation:

- 📊 **Window Functions** - Analytics and rankings with OVER clauses
- 🔗 **CTEs (WITH clauses)** - Organize complex queries
- 🔀 **CASE Expressions** - Conditional logic in queries
- 📈 **Advanced Aggregates** - FILTER, DISTINCT, STRING_AGG
- ⚡ **NULL Handling** - COALESCE, NULLIF
- 🎯 **Type-Safe DSL** - Compile-time safety for all operations

---

## Window Functions

Window functions perform calculations across rows related to the current row.

### Basic Window Functions

```kotlin
import com.korm.dsl.query.window.*

// ROW_NUMBER() - Sequential number for each row
val rowNum = WindowFunctions.rowNumber()

// RANK() - Rank with gaps for ties
val rank = WindowFunctions.rank()

// DENSE_RANK() - Rank without gaps
val denseRank = WindowFunctions.denseRank()
```

### Window Specification (OVER Clause)

```kotlin
// PARTITION BY - Group rows
val spec = window {
    partitionBy(Users.department)
    orderBy(Users.salary, ascending = false)
}

// Apply to window function
val rankedSalaries = WindowFunctions.rowNumber() over spec
```

### Complete Example

```sql
-- SQL equivalent
SELECT 
    name,
    department,
    salary,
    ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) as rank
FROM employees
```

```kotlin
// KORM usage (raw SQL for now, DSL integration coming)
val sql = """
    SELECT 
        name,
        department,
        salary,
        ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) as dept_rank
    FROM employees
    ORDER BY department, dept_rank
""".trimIndent()

db.useConnection { conn ->
    conn.createStatement().use { stmt ->
        val rs = stmt.executeQuery(sql)
        while (rs.next()) {
            val name = rs.getString("name")
            val dept = rs.getString("department")
            val salary = rs.getInt("salary")
            val rank = rs.getInt("dept_rank")
            println("$name ($dept): $$salary - Rank #$rank")
        }
    }
}
```

### LAG and LEAD Functions

Compare rows with previous/next rows:

```kotlin
// LAG - Get value from previous row
val lag = WindowFunctions.lag(Sales.amount, offset = 1)

// LEAD - Get value from next row
val lead = WindowFunctions.lead(Sales.amount, offset = 1)

// With default value
val lagWithDefault = WindowFunctions.lag(Sales.amount, offset = 1, default = 0)
```

**Example:**

```sql
SELECT 
    product_name,
    amount,
    LAG(amount) OVER (ORDER BY id) as prev_amount,
    amount - LAG(amount) OVER (ORDER BY id) as difference
FROM sales
```

### FIRST_VALUE and LAST_VALUE

```kotlin
// FIRST_VALUE - First value in window
val first = WindowFunctions.firstValue(Sales.amount)

// LAST_VALUE - Last value in window
val last = WindowFunctions.lastValue(Sales.amount)
```

### Running Totals and Aggregates

```sql
SELECT 
    date,
    amount,
    SUM(amount) OVER (ORDER BY date) as running_total,
    AVG(amount) OVER (ORDER BY date ROWS BETWEEN 2 PRECEDING AND CURRENT ROW) as moving_avg
FROM sales
```

### Frame Clauses

```kotlin
val spec = window {
    orderBy(Sales.date)
    frame("ROWS BETWEEN 2 PRECEDING AND CURRENT ROW")
}
```

Common frame specifications:
- `ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW` - All previous rows
- `ROWS BETWEEN 2 PRECEDING AND 2 FOLLOWING` - 2 rows before and after
- `RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING` - Entire partition

---

## CASE Expressions

### Simple CASE

```kotlin
val level = caseWhen(Employees.salary)
    .whenever(90000..Int.MAX_VALUE, "Senior")
    .whenever(75000..89999, "Mid-Level")
    .otherwise("Junior")
```

**SQL:**

```sql
CASE 
    WHEN salary >= 90000 THEN 'Senior'
    WHEN salary >= 75000 THEN 'Mid-Level'
    ELSE 'Junior'
END as level
```

### Searched CASE

```kotlin
val category = caseWhen()
    .whenever("salary >= 90000", "High")
    .whenever("salary >= 70000", "Medium")
    .otherwise("Low")
```

### CASE in SELECT

```sql
SELECT 
    name,
    salary,
    CASE 
        WHEN salary >= 90000 THEN 'Senior'
        WHEN salary >= 75000 THEN 'Mid-Level'
        ELSE 'Junior'
    END as level
FROM employees
```

### CASE with Aggregation

```sql
SELECT 
    department,
    SUM(CASE WHEN salary >= 90000 THEN 1 ELSE 0 END) as senior_count,
    SUM(CASE WHEN salary >= 75000 AND salary < 90000 THEN 1 ELSE 0 END) as mid_count,
    SUM(CASE WHEN salary < 75000 THEN 1 ELSE 0 END) as junior_count
FROM employees
GROUP BY department
```

### Nested CASE

```sql
SELECT 
    name,
    CASE department
        WHEN 'Engineering' THEN 
            CASE 
                WHEN salary > 100000 THEN 'Senior Engineer'
                ELSE 'Engineer'
            END
        WHEN 'Sales' THEN 'Sales Rep'
        ELSE 'Other'
    END as title
FROM employees
```

---

## Advanced Aggregates

### FILTER Clause

Apply conditions to aggregates:

```sql
SELECT 
    department,
    COUNT(*) as total,
    COUNT(*) FILTER (WHERE salary >= 80000) as high_earners,
    AVG(salary) as avg_all,
    AVG(salary) FILTER (WHERE salary >= 80000) as avg_high
FROM employees
GROUP BY department
```

```kotlin
// Using FILTER
val filtered = Employees.count() filter "salary >= 80000"
```

### STRING_AGG / GROUP_CONCAT

Concatenate strings:

**PostgreSQL:**

```sql
SELECT 
    department,
    STRING_AGG(name, ', ' ORDER BY name) as employees
FROM employees
GROUP BY department
```

**MySQL:**

```sql
SELECT 
    department,
    GROUP_CONCAT(name ORDER BY name SEPARATOR ', ') as employees
FROM employees
GROUP BY department
```

**H2:**

```sql
SELECT 
    department,
    LISTAGG(name, ', ') WITHIN GROUP (ORDER BY name) as employees
FROM employees
GROUP BY department
```

```kotlin
// KORM DSL
val names = StringAggregates.stringAgg(Employees.name, separator = ", ")
    .orderBy(Employees.name)
```

### DISTINCT in Aggregates

```sql
SELECT 
    COUNT(DISTINCT department) as dept_count,
    STRING_AGG(DISTINCT department, ', ') as departments
FROM employees
```

### ARRAY_AGG (PostgreSQL)

```sql
SELECT 
    department,
    ARRAY_AGG(name ORDER BY salary DESC) as employees
FROM employees
GROUP BY department
```

```kotlin
val array = arrayAgg(Employees.name)
    .orderBy(Employees.salary, ascending = false)
```

### Statistical Aggregates

```kotlin
// Standard deviation
val stddev = StatisticalAggregates.stddev(Employees.salary)

// Variance
val variance = StatisticalAggregates.variance(Employees.salary)

// Percentiles
val median = StatisticalAggregates.percentileCont(0.5)
    .withinGroup(Employees.salary)
```

**SQL:**

```sql
SELECT 
    department,
    STDDEV(salary) as salary_stddev,
    VARIANCE(salary) as salary_variance,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY salary) as median_salary,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY salary) as p95_salary
FROM employees
GROUP BY department
```

---

## NULL Handling

### COALESCE

Return first non-NULL value:

```kotlin
val value = coalesce(Employees.bonus, 0)
```

**SQL:**

```sql
SELECT 
    name,
    COALESCE(bonus, 0) as bonus,
    COALESCE(department, 'Unassigned') as department
FROM employees
```

### NULLIF

Convert value to NULL:

```kotlin
val nullified = nullIf(Employees.department, "Unknown")
```

**SQL:**

```sql
SELECT 
    name,
    NULLIF(department, 'Unknown') as department
FROM employees
```

### Combining COALESCE and NULLIF

```sql
SELECT 
    name,
    COALESCE(NULLIF(department, ''), 'No Department') as department
FROM employees
```

---

## CTEs (Common Table Expressions)

### Basic CTE

```sql
WITH high_earners AS (
    SELECT * FROM employees WHERE salary >= 90000
)
SELECT department, COUNT(*) as count
FROM high_earners
GROUP BY department
```

### Multiple CTEs

```sql
WITH 
    high_earners AS (
        SELECT * FROM employees WHERE salary >= 90000
    ),
    dept_stats AS (
        SELECT 
            department,
            AVG(salary) as avg_salary
        FROM employees
        GROUP BY department
    )
SELECT 
    he.name,
    he.salary,
    ds.avg_salary
FROM high_earners he
JOIN dept_stats ds ON he.department = ds.department
```

### Recursive CTEs

Hierarchical data:

```sql
WITH RECURSIVE org_chart AS (
    -- Base case: top-level employees
    SELECT 
        id, 
        name, 
        manager_id,
        1 as level
    FROM employees
    WHERE manager_id IS NULL
    
    UNION ALL
    
    -- Recursive case: subordinates
    SELECT 
        e.id,
        e.name,
        e.manager_id,
        oc.level + 1
    FROM employees e
    JOIN org_chart oc ON e.manager_id = oc.id
)
SELECT * FROM org_chart
ORDER BY level, name
```

---

## Real-World Examples

### Employee Ranking and Analysis

```sql
SELECT 
    name,
    department,
    salary,
    ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) as dept_rank,
    RANK() OVER (ORDER BY salary DESC) as overall_rank,
    salary - AVG(salary) OVER (PARTITION BY department) as diff_from_dept_avg,
    ROUND(100.0 * salary / SUM(salary) OVER (PARTITION BY department), 2) as pct_of_dept_payroll,
    CASE 
        WHEN salary >= 90000 THEN 'Senior'
        WHEN salary >= 75000 THEN 'Mid-Level'
        ELSE 'Junior'
    END as level
FROM employees
ORDER BY department, dept_rank
```

### Sales Analysis with Trends

```sql
SELECT 
    date,
    product,
    amount,
    SUM(amount) OVER (PARTITION BY product ORDER BY date) as cumulative_sales,
    AVG(amount) OVER (
        PARTITION BY product 
        ORDER BY date 
        ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) as moving_avg_7day,
    amount - LAG(amount) OVER (PARTITION BY product ORDER BY date) as day_over_day_change,
    RANK() OVER (PARTITION BY product ORDER BY amount DESC) as best_day_rank
FROM sales
ORDER BY product, date
```

### Top N per Group

```sql
WITH ranked_sales AS (
    SELECT 
        product,
        region,
        amount,
        ROW_NUMBER() OVER (PARTITION BY region ORDER BY amount DESC) as rank
    FROM sales
)
SELECT 
    product,
    region,
    amount
FROM ranked_sales
WHERE rank <= 3
ORDER BY region, rank
```

### Gap and Island Detection

Find consecutive sequences:

```sql
WITH gaps AS (
    SELECT 
        date,
        amount,
        LAG(date) OVER (ORDER BY date) as prev_date,
        date - LAG(date) OVER (ORDER BY date) as gap_days
    FROM sales
)
SELECT 
    date,
    gap_days,
    CASE 
        WHEN gap_days > 1 THEN 'Gap detected'
        ELSE 'Consecutive'
    END as status
FROM gaps
WHERE gap_days IS NOT NULL
```

---

## Database Compatibility

### Window Functions

| Function | PostgreSQL | MySQL | SQLite | H2 |
|----------|-----------|-------|--------|-----|
| ROW_NUMBER | ✅ | ✅ | ✅ | ✅ |
| RANK | ✅ | ✅ | ✅ | ✅ |
| DENSE_RANK | ✅ | ✅ | ✅ | ✅ |
| LAG/LEAD | ✅ | ✅ | ✅ | ✅ |
| FIRST_VALUE | ✅ | ✅ | ✅ | ✅ |
| LAST_VALUE | ✅ | ✅ | ✅ | ✅ |

### Advanced Aggregates

| Feature | PostgreSQL | MySQL | SQLite | H2 |
|---------|-----------|-------|--------|-----|
| FILTER clause | ✅ | ❌ | ❌ | ✅ |
| STRING_AGG | ✅ | ❌* | ❌ | ❌ |
| GROUP_CONCAT | ❌ | ✅ | ✅ | ❌ |
| LISTAGG | ❌ | ❌ | ❌ | ✅ |
| ARRAY_AGG | ✅ | ❌ | ❌ | ✅ |

*MySQL uses GROUP_CONCAT instead

### CTEs

| Feature | PostgreSQL | MySQL | SQLite | H2 |
|---------|-----------|-------|--------|-----|
| Basic CTE | ✅ | ✅ | ✅ | ✅ |
| Recursive CTE | ✅ | ✅ | ✅ | ✅ |

---

## Best Practices

### 1. Use Window Functions for Analytics

**❌ Bad - Self-join:**

```sql
SELECT 
    e1.name,
    e1.salary,
    COUNT(e2.id) as rank
FROM employees e1
LEFT JOIN employees e2 ON e1.department = e2.department 
    AND e2.salary >= e1.salary
GROUP BY e1.id, e1.name, e1.salary
```

**✅ Good - Window function:**

```sql
SELECT 
    name,
    salary,
    RANK() OVER (PARTITION BY department ORDER BY salary DESC) as rank
FROM employees
```

### 2. Use CTEs for Readability

**❌ Bad - Nested subqueries:**

```sql
SELECT * FROM (
    SELECT * FROM (
        SELECT * FROM employees WHERE salary > 80000
    ) WHERE department = 'Engineering'
) WHERE hire_date > '2023-01-01'
```

**✅ Good - CTE:**

```sql
WITH high_earners AS (
    SELECT * FROM employees WHERE salary > 80000
),
engineers AS (
    SELECT * FROM high_earners WHERE department = 'Engineering'
)
SELECT * FROM engineers WHERE hire_date > '2023-01-01'
```

### 3. Use CASE for Conditional Aggregation

**❌ Bad - Multiple queries:**

```kotlin
val seniorCount = Employees.select(db)
    .where("salary >= 90000")
    .execute { rs -> 1 }.size

val midCount = Employees.select(db)
    .where("salary >= 75000 AND salary < 90000")
    .execute { rs -> 1 }.size
```

**✅ Good - Single query with CASE:**

```sql
SELECT 
    SUM(CASE WHEN salary >= 90000 THEN 1 ELSE 0 END) as senior_count,
    SUM(CASE WHEN salary >= 75000 AND salary < 90000 THEN 1 ELSE 0 END) as mid_count
FROM employees
```

### 4. Use FILTER for Cleaner Code

**❌ Bad - CASE in aggregate:**

```sql
SELECT 
    department,
    AVG(CASE WHEN salary >= 80000 THEN salary ELSE NULL END) as avg_high_salary
FROM employees
GROUP BY department
```

**✅ Good - FILTER clause:**

```sql
SELECT 
    department,
    AVG(salary) FILTER (WHERE salary >= 80000) as avg_high_salary
FROM employees
GROUP BY department
```

### 5. Use COALESCE for Defaults

**❌ Bad - NULL checks:**

```sql
SELECT 
    name,
    CASE WHEN bonus IS NULL THEN 0 ELSE bonus END as bonus
FROM employees
```

**✅ Good - COALESCE:**

```sql
SELECT 
    name,
    COALESCE(bonus, 0) as bonus
FROM employees
```

---

## Performance Considerations

### Window Functions

- **Index ORDER BY columns** - Improves window function performance
- **Limit partitions** - Too many partitions can slow down queries
- **Frame clauses** - Can be expensive for large windows

### CTEs

- **Materialization** - CTEs may be materialized (temporary table)
- **Optimization** - Modern databases optimize CTEs well
- **Recursive CTEs** - Can be slow without proper termination

### CASE Expressions

- **Short-circuit evaluation** - Conditions evaluated in order
- **Indexing** - CASE results cannot use indexes
- **Simplicity** - Keep CASE expressions simple

---

## Next Steps

- **[Window Functions in Production](window-functions-production.md)** (Coming Soon)
- **[CTE Optimization](cte-optimization.md)** (Coming Soon)
- **[Advanced Analytics](advanced-analytics.md)** (Coming Soon)

---

## Complete Example

```sql
-- Comprehensive employee analysis
WITH 
    dept_stats AS (
        SELECT 
            department,
            AVG(salary) as avg_salary,
            COUNT(*) as emp_count
        FROM employees
        GROUP BY department
    ),
    ranked_employees AS (
        SELECT 
            e.name,
            e.department,
            e.salary,
            ROW_NUMBER() OVER (PARTITION BY e.department ORDER BY e.salary DESC) as dept_rank,
            CASE 
                WHEN e.salary >= 90000 THEN 'Senior'
                WHEN e.salary >= 75000 THEN 'Mid-Level'
                ELSE 'Junior'
            END as level,
            e.salary - ds.avg_salary as diff_from_avg,
            LAG(e.salary) OVER (PARTITION BY e.department ORDER BY e.salary) as prev_salary
        FROM employees e
        JOIN dept_stats ds ON e.department = ds.department
    )
SELECT 
    name,
    department,
    salary,
    level,
    dept_rank,
    diff_from_avg,
    COALESCE(salary - prev_salary, 0) as salary_increase_vs_prev
FROM ranked_employees
WHERE dept_rank <= 5
ORDER BY department, dept_rank
```

This combines:
- ✅ CTEs for organization
- ✅ Window functions for ranking
- ✅ CASE for categorization
- ✅ Aggregates for statistics
- ✅ COALESCE for NULL handling
