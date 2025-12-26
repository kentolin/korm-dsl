package com.korm.examples.advancedqueries

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.query.*
import com.korm.dsl.query.expressions.*
import com.korm.dsl.query.window.*
import com.korm.dsl.schema.Table
import com.korm.dsl.schema.create


// Schema definitions
object Employees : Table("employees") {
    val id = int("id").autoIncrement().primaryKey()
    val name = varchar("name", 100).notNull()
    val department = varchar("department", 50).notNull()
    val salary = int("salary").notNull()
    val hireDate = varchar("hire_date", 20).notNull()
}

object Sales : Table("sales") {
    val id = int("id").autoIncrement().primaryKey()
    val productName = varchar("product_name", 100).notNull()
    val amount = int("amount").notNull()
    val saleDate = varchar("sale_date", 20).notNull()
    val region = varchar("region", 50).notNull()
}

fun main() {
    println("=== KORM Advanced Queries Examples ===\n")

    // Setup database
    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:advanced_demo;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    try {
        // Create schema and sample data
        setupData(db)

        // Run examples
        example1_windowFunctions(db)
        example2_caseExpressions(db)
        example3_advancedAggregates(db)
        example4_nullHandling(db)
        example5_complexQueries(db)

    } finally {
        db.close()
    }
}

/**
 * Setup sample data
 */
fun setupData(db: Database) {
    println("Setting up sample data...\n")

    // Create tables
    Employees.create(db)
    Sales.create(db)

    // Insert employees
    val employees = listOf(
        Triple("Alice Johnson", "Engineering", 95000),
        Triple("Bob Smith", "Engineering", 85000),
        Triple("Carol Davis", "Engineering", 90000),
        Triple("David Brown", "Sales", 75000),
        Triple("Eve Wilson", "Sales", 80000),
        Triple("Frank Miller", "Sales", 70000),
        Triple("Grace Lee", "HR", 65000),
        Triple("Henry Clark", "HR", 68000)
    )

    employees.forEach { (name, dept, salary) ->
        Employees.insert(db)
            .set(Employees.name, name)
            .set(Employees.department, dept)
            .set(Employees.salary, salary)
            .set(Employees.hireDate, "2024-01-15")
            .execute()
    }

    // Insert sales
    val sales = listOf(
        Triple("Laptop", 1200, "North"),
        Triple("Phone", 800, "North"),
        Triple("Tablet", 600, "North"),
        Triple("Laptop", 1200, "South"),
        Triple("Phone", 800, "South"),
        Triple("Monitor", 400, "East"),
        Triple("Keyboard", 100, "East"),
        Triple("Mouse", 50, "West")
    )

    sales.forEach { (product, amount, region) ->
        Sales.insert(db)
            .set(Sales.productName, product)
            .set(Sales.amount, amount)
            .set(Sales.saleDate, "2024-12-01")
            .set(Sales.region, region)
            .execute()
    }

    println("✓ Sample data created\n")
}

/**
 * Example 1: Window Functions
 */
fun example1_windowFunctions(db: Database) {
    println("━━━ Example 1: Window Functions ━━━\n")

    // Example 1a: ROW_NUMBER with partitioning
    println("1a. Employee ranking by salary within each department:")
    println("   ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC)\n")

    val rankingSQL = """
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
            val rs = stmt.executeQuery(rankingSQL)
            while (rs.next()) {
                println("   ${rs.getString("name").padEnd(20)} | " +
                    "${rs.getString("department").padEnd(12)} | " +
                    "$${rs.getInt("salary")} | " +
                    "Rank: ${rs.getInt("dept_rank")}")
            }
        }
    }

    // Example 1b: Running total
    println("\n1b. Running total of sales by region:")
    println("   SUM(amount) OVER (PARTITION BY region ORDER BY id)\n")

    val runningTotalSQL = """
        SELECT
            product_name,
            region,
            amount,
            SUM(amount) OVER (PARTITION BY region ORDER BY id) as running_total
        FROM sales
        ORDER BY region, id
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(runningTotalSQL)
            while (rs.next()) {
                println("   ${rs.getString("product_name").padEnd(15)} | " +
                    "${rs.getString("region").padEnd(10)} | " +
                    "$${rs.getInt("amount").toString().padEnd(6)} | " +
                    "Running Total: $${rs.getInt("running_total")}")
            }
        }
    }

    // Example 1c: LAG for comparing with previous row
    println("\n1c. Salary compared to previous employee in department:")
    println("   LAG(salary) OVER (PARTITION BY department ORDER BY salary)\n")

    val lagSQL = """
        SELECT
            name,
            department,
            salary,
            LAG(salary) OVER (PARTITION BY department ORDER BY salary) as prev_salary,
            salary - COALESCE(LAG(salary) OVER (PARTITION BY department ORDER BY salary), salary) as diff
        FROM employees
        ORDER BY department, salary
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(lagSQL)
            while (rs.next()) {
                val prevSalary = rs.getObject("prev_salary")
                val diff = rs.getInt("diff")
                println("   ${rs.getString("name").padEnd(20)} | " +
                    "${rs.getString("department").padEnd(12)} | " +
                    "$${rs.getInt("salary")} | " +
                    "Prev: ${if (prevSalary != null) "$$prevSalary" else "N/A".padEnd(8)} | " +
                    "Diff: ${if (diff > 0) "+$diff" else "$diff"}")
            }
        }
    }

    println()
}

/**
 * Example 2: CASE Expressions
 */
fun example2_caseExpressions(db: Database) {
    println("━━━ Example 2: CASE Expressions ━━━\n")

    // Example 2a: Simple CASE
    println("2a. Categorize employees by salary level:")

    val simpleCaseSQL = """
        SELECT
            name,
            salary,
            CASE
                WHEN salary >= 90000 THEN 'Senior'
                WHEN salary >= 75000 THEN 'Mid-Level'
                ELSE 'Junior'
            END as level
        FROM employees
        ORDER BY salary DESC
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(simpleCaseSQL)
            while (rs.next()) {
                println("   ${rs.getString("name").padEnd(20)} | " +
                    "$${rs.getInt("salary").toString().padEnd(8)} | " +
                    rs.getString("level"))
            }
        }
    }

    // Example 2b: CASE with aggregation
    println("\n2b. Count employees by salary level per department:")

    val caseAggSQL = """
        SELECT
            department,
            SUM(CASE WHEN salary >= 90000 THEN 1 ELSE 0 END) as senior_count,
            SUM(CASE WHEN salary >= 75000 AND salary < 90000 THEN 1 ELSE 0 END) as mid_count,
            SUM(CASE WHEN salary < 75000 THEN 1 ELSE 0 END) as junior_count
        FROM employees
        GROUP BY department
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(caseAggSQL)
            while (rs.next()) {
                println("   ${rs.getString("department").padEnd(15)} | " +
                    "Senior: ${rs.getInt("senior_count")} | " +
                    "Mid: ${rs.getInt("mid_count")} | " +
                    "Junior: ${rs.getInt("junior_count")}")
            }
        }
    }

    // Example 2c: COALESCE
    println("\n2c. Using COALESCE for default values:")

    val coalesceSQL = """
        SELECT
            name,
            COALESCE(department, 'Unassigned') as department,
            salary
        FROM employees
        LIMIT 3
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(coalesceSQL)
            while (rs.next()) {
                println("   ${rs.getString("name").padEnd(20)} | " +
                    rs.getString("department"))
            }
        }
    }

    println()
}

/**
 * Example 3: Advanced Aggregates
 */
fun example3_advancedAggregates(db: Database) {
    println("━━━ Example 3: Advanced Aggregates ━━━\n")

    // Example 3a: FILTER clause
    println("3a. Conditional aggregation with FILTER:")

    val filterSQL = """
        SELECT
            department,
            COUNT(*) as total_employees,
            COUNT(*) FILTER (WHERE salary >= 80000) as high_earners,
            AVG(salary) as avg_salary,
            AVG(salary) FILTER (WHERE salary >= 80000) as avg_high_salary
        FROM employees
        GROUP BY department
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(filterSQL)
            while (rs.next()) {
                val highSalaryAvg = rs.getObject("avg_high_salary")
                println("   ${rs.getString("department").padEnd(15)} | " +
                    "Total: ${rs.getInt("total_employees")} | " +
                    "High Earners: ${rs.getInt("high_earners")} | " +
                    "Avg All: $${rs.getInt("avg_salary")} | " +
                    "Avg High: ${if (highSalaryAvg != null) "$$highSalaryAvg" else "N/A"}")
            }
        }
    }

    // Example 3b: String aggregation
    println("\n3b. String aggregation (GROUP_CONCAT/STRING_AGG):")

    // H2 supports LISTAGG or GROUP_CONCAT
    val stringAggSQL = """
        SELECT
            department,
            LISTAGG(name, ', ') WITHIN GROUP (ORDER BY name) as employees
        FROM employees
        GROUP BY department
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(stringAggSQL)
            while (rs.next()) {
                println("   ${rs.getString("department").padEnd(15)} | ${rs.getString("employees")}")
            }
        }
    }

    // Example 3c: Multiple aggregates
    println("\n3c. Multiple statistical aggregates:")

    val statsSQL = """
        SELECT
            department,
            COUNT(*) as count,
            MIN(salary) as min_salary,
            MAX(salary) as max_salary,
            AVG(salary) as avg_salary,
            MAX(salary) - MIN(salary) as salary_range
        FROM employees
        GROUP BY department
        ORDER BY avg_salary DESC
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(statsSQL)
            while (rs.next()) {
                println("   ${rs.getString("department").padEnd(15)} | " +
                    "Count: ${rs.getInt("count")} | " +
                    "Min: $${rs.getInt("min_salary")} | " +
                    "Max: $${rs.getInt("max_salary")} | " +
                    "Avg: $${rs.getInt("avg_salary")} | " +
                    "Range: $${rs.getInt("salary_range")}")
            }
        }
    }

    println()
}

/**
 * Example 4: NULL Handling
 */
fun example4_nullHandling(db: Database) {
    println("━━━ Example 4: NULL Handling ━━━\n")

    println("4a. COALESCE for providing default values:")

    val coalesceSQL = """
        SELECT
            name,
            COALESCE(NULLIF(department, ''), 'No Department') as department,
            salary
        FROM employees
        LIMIT 5
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(coalesceSQL)
            while (rs.next()) {
                println("   ${rs.getString("name").padEnd(20)} | " +
                    rs.getString("department").padEnd(15) + " | " +
                    "$${rs.getInt("salary")}")
            }
        }
    }

    println("\n4b. NULLIF to convert values to NULL:")
    println("   (Convert empty strings to NULL)\n")

    val nullifSQL = """
        SELECT
            name,
            NULLIF(department, 'Unknown') as department
        FROM employees
        LIMIT 3
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(nullifSQL)
            while (rs.next()) {
                val dept = rs.getString("department")
                println("   ${rs.getString("name").padEnd(20)} | " +
                    "${if (dept != null) dept else "NULL"}")
            }
        }
    }

    println()
}

/**
 * Example 5: Complex Queries Combining Features
 */
fun example5_complexQueries(db: Database) {
    println("━━━ Example 5: Complex Queries ━━━\n")

    // Combine window functions, CASE, and aggregates
    println("5a. Comprehensive employee analysis:")

    val complexSQL = """
        SELECT
            name,
            department,
            salary,
            CASE
                WHEN salary >= 90000 THEN 'Senior'
                WHEN salary >= 75000 THEN 'Mid-Level'
                ELSE 'Junior'
            END as level,
            ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) as dept_rank,
            salary - AVG(salary) OVER (PARTITION BY department) as diff_from_avg,
            ROUND(100.0 * salary / SUM(salary) OVER (PARTITION BY department), 1) as pct_of_dept_payroll
        FROM employees
        ORDER BY department, dept_rank
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(complexSQL)
            while (rs.next()) {
                println("   ${rs.getString("name").padEnd(20)} | " +
                    "${rs.getString("department").padEnd(12)} | " +
                    "$${rs.getInt("salary").toString().padEnd(6)} | " +
                    "${rs.getString("level").padEnd(10)} | " +
                    "Rank: ${rs.getInt("dept_rank")} | " +
                    "Diff: ${rs.getInt("diff_from_avg")} | " +
                    "% of payroll: ${rs.getDouble("pct_of_dept_payroll")}%")
            }
        }
    }

    println("\n5b. Sales analysis with multiple window functions:")

    val salesAnalysisSQL = """
        SELECT
            product_name,
            region,
            amount,
            ROW_NUMBER() OVER (ORDER BY amount DESC) as overall_rank,
            RANK() OVER (PARTITION BY region ORDER BY amount DESC) as region_rank,
            SUM(amount) OVER (PARTITION BY region) as region_total,
            ROUND(100.0 * amount / SUM(amount) OVER (PARTITION BY region), 1) as pct_of_region
        FROM sales
        ORDER BY region, region_rank
    """.trimIndent()

    db.useConnection { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery(salesAnalysisSQL)
            while (rs.next()) {
                println("   ${rs.getString("product_name").padEnd(15)} | " +
                    "${rs.getString("region").padEnd(10)} | " +
                    "$${rs.getInt("amount").toString().padEnd(6)} | " +
                    "Overall: #${rs.getInt("overall_rank")} | " +
                    "Region: #${rs.getInt("region_rank")} | " +
                    "Regional Total: $${rs.getInt("region_total")} | " +
                    "${rs.getDouble("pct_of_region")}%")
            }
        }
    }

    println()
    println("✓ Advanced queries examples completed!")
}
