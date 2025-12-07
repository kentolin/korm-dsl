// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/migrations/Migrations.kt

package com.korm.examples.enterprise.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.migrations.Migration
import com.korm.dsl.migrations.MigrationEngine
import com.korm.dsl.migrations.AbstractMigration
import com.korm.examples.enterprise.models.*

fun runMigrations(database: Database) {
    val migrations = listOf(
        Migration1_CreateUsers(),
        Migration2_CreateProducts(),
        Migration3_CreateOrders(),
        Migration4_CreateAuditLogs(),
        Migration5_AddIndexes()
    )

    val engine = MigrationEngine(database)
    migrations.forEach { migration ->
        engine.migrate(migration)
    }
}

class Migration1_CreateUsers : AbstractMigration(
    version = 1,
    description = "Create users table"
) {
    override fun up(database: Database) {
        database.createTables(Users)
    }

    override fun down(database: Database) {
        database.dropTables(Users)
    }
}

class Migration2_CreateProducts : AbstractMigration(
    version = 2,
    description = "Create products table"
) {
    override fun up(database: Database) {
        database.createTables(Products)
    }

    override fun down(database: Database) {
        database.dropTables(Products)
    }
}

class Migration3_CreateOrders : AbstractMigration(
    version = 3,
    description = "Create orders and order_items tables"
) {
    override fun up(database: Database) {
        database.createTables(Orders, OrderItems)
    }

    override fun down(database: Database) {
        database.dropTables(OrderItems, Orders)
    }
}

class Migration4_CreateAuditLogs : AbstractMigration(
    version = 4,
    description = "Create audit_logs table"
) {
    override fun up(database: Database) {
        database.createTables(AuditLogs)
    }

    override fun down(database: Database) {
        database.dropTables(AuditLogs)
    }
}

class Migration5_AddIndexes : AbstractMigration(
    version = 5,
    description = "Add performance indexes"
) {
    override fun up(database: Database) {
        database.transaction {
            execute("""
                CREATE INDEX IF NOT EXISTS idx_orders_user_status
                ON orders(user_id, status)
            """)

            execute("""
                CREATE INDEX IF NOT EXISTS idx_products_category_active
                ON products(category, active)
            """)

            execute("""
                CREATE INDEX IF NOT EXISTS idx_audit_logs_entity
                ON audit_logs(entity_type, entity_id, created_at)
            """)
        }
    }

    override fun down(database: Database) {
        database.transaction {
            execute("DROP INDEX IF EXISTS idx_orders_user_status")
            execute("DROP INDEX IF EXISTS idx_products_category_active")
            execute("DROP INDEX IF EXISTS idx_audit_logs_entity")
        }
    }
}
