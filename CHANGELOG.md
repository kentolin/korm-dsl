# Changelog

All notable changes to KORM DSL will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned
- Schema migrations
- Query result caching
- Monitoring and profiling
- Window functions
- CTEs (Common Table Expressions)
- Multiplatform support

---

## [0.1.0] - 2024-12-25

### Added

#### Core Features
- **Connection Management**
    - HikariCP-based connection pooling
    - Multiple database dialect support (PostgreSQL, MySQL, SQLite, H2)
    - Automatic resource management

- **Schema Definition**
    - Type-safe table definitions
    - Column types: int, long, varchar, text, boolean, double
    - Constraints: primaryKey, autoIncrement, notNull, unique, default
    - Foreign key support
    - Table creation and dropping

- **Query Builders**
    - SELECT queries with WHERE, LIMIT, OFFSET, ORDER BY
    - INSERT queries
    - UPDATE queries with WHERE conditions
    - DELETE queries with WHERE conditions
    - Type-safe column selection
    - Raw query support

- **JOINs**
    - INNER JOIN
    - LEFT JOIN
    - RIGHT JOIN
    - FULL OUTER JOIN
    - Multiple table JOINs
    - Lambda and direct column specification styles

- **Aggregations**
    - COUNT, SUM, AVG, MIN, MAX
    - GROUP BY support
    - HAVING clauses
    - Multiple aggregates in single query
    - DISTINCT support

- **Batch Operations**
    - Batch INSERT with configurable batch size
    - Batch UPDATE
    - Efficient bulk data operations

- **Subqueries**
    - WHERE IN (subquery)
    - WHERE NOT IN
    - WHERE EXISTS
    - WHERE NOT EXISTS

- **UNION Queries**
    - UNION (distinct)
    - UNION ALL

- **Transactions**
    - Automatic commit/rollback
    - Exception-based rollback
    - Transaction DSL

- **Validation Framework**
    - NotNull validation
    - StringLength validation
    - Email validation
    - Numeric range validation
    - Pattern (regex) validation
    - OneOf (enum-like) validation
    - Custom validation rules
    - ValidationContext for multi-field validation
    - ValidationException

#### Examples
- `example-basic` - CRUD operations
- `example-relationships` - JOINs and foreign keys
- `example-aggregates` - GROUP BY and aggregations
- `example-advanced` - Validation, batch operations, complex queries

#### Documentation
- Comprehensive README
- Quick Start Guide
- Core Concepts documentation
    - Table Definition
    - Queries
    - Relationships & JOINs
    - Transactions
- Advanced Features documentation
    - Validation
    - Performance
- CONTRIBUTING guide
- Apache 2.0 LICENSE

### Database Support
- PostgreSQL 12+
- MySQL 8+
- SQLite 3+
- H2 2.x

### Dependencies
- Kotlin 2.1.0
- HikariCP 6.2.1
- SLF4J 2.0.16

---

## Version History

- **0.1.0** - Initial release with core ORM functionality

---

## Contributors

Thank you to all contributors who helped make KORM DSL possible!

- Padam - Project creator and maintainer

---

## Migration Guides

### Upgrading to 0.1.0

This is the initial release. No migration needed.

---

## Notes

### Breaking Changes

None - this is the first release.

### Deprecations

None.

### Known Issues

- Transaction isolation levels not yet supported
- Savepoints not yet supported
- No built-in caching layer
- Limited to JVM platform (multiplatform support planned)

---

[Unreleased]: https://github.com/yourusername/korm-dsl/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/yourusername/korm-dsl/releases/tag/v0.1.0
