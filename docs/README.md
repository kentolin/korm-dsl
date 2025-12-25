# KORM DSL Documentation

Complete documentation for KORM DSL - Kotlin Object-Relational Mapping framework.

---

## 📚 Documentation Structure

### Getting Started
- **[Quick Start](getting-started/quick-start.md)** - Get up and running in 5 minutes

### Core Concepts
- **[Table Definition](core-concepts/table-definition.md)** - Define database schemas
- **[Queries](core-concepts/queries.md)** - SELECT, INSERT, UPDATE, DELETE operations
- **[Relationships](core-concepts/relationships.md)** - JOINs and foreign keys
- **[Transactions](core-concepts/transactions.md)** - Transaction management
- **[Migrations](core-concepts/migrations.md)** - Schema migration system

### Advanced Features
- **[Validation](advanced/validation.md)** - Data validation framework
- **[Performance](advanced/performance.md)** - Optimization techniques

---

## 🎯 Quick Links

### For Beginners
1. [Installation](getting-started/quick-start.md#installation)
2. [Your First Application](getting-started/quick-start.md#your-first-korm-application)
3. [Basic CRUD Operations](core-concepts/queries.md#select-queries)

### For Advanced Users
1. [Complex JOINs](core-concepts/relationships.md#multiple-joins)
2. [Batch Operations](advanced/performance.md#batch-operations)
3. [Transaction Patterns](core-concepts/transactions.md#real-world-examples)

### Reference
- [Complete API Documentation](#) (Coming Soon)
- [Migration Guides](../CHANGELOG.md)
- [Contributing Guide](../CONTRIBUTING.md)

---

## 📖 Documentation by Topic

### Database Connection
- [Connection Pooling](getting-started/quick-start.md#database-configuration)
- [Database Dialects](core-concepts/table-definition.md#working-with-multiple-databases)
- [Resource Management](advanced/performance.md#best-practices)

### Schema & Tables
- [Table Definition](core-concepts/table-definition.md#basic-table-definition)
- [Column Types](core-concepts/table-definition.md#column-types)
- [Constraints](core-concepts/table-definition.md#column-constraints)
- [Foreign Keys](core-concepts/table-definition.md#foreign-keys)

### Queries
- [SELECT Queries](core-concepts/queries.md#select-queries)
- [INSERT Operations](core-concepts/queries.md#insert-queries)
- [UPDATE Operations](core-concepts/queries.md#update-queries)
- [DELETE Operations](core-concepts/queries.md#delete-queries)
- [Aggregations](core-concepts/queries.md#aggregates)
- [Subqueries](core-concepts/queries.md#subqueries)

### JOINs & Relationships
- [INNER JOIN](core-concepts/relationships.md#inner-join)
- [LEFT JOIN](core-concepts/relationships.md#left-join)
- [Multiple JOINs](core-concepts/relationships.md#multiple-joins)
- [JOINs with Aggregates](core-concepts/relationships.md#joins-with-aggregates)

### Transactions
- [Basic Transactions](core-concepts/transactions.md#basic-transaction)
- [Error Handling](core-concepts/transactions.md#error-handling)
- [Best Practices](core-concepts/transactions.md#best-practices)

### Validation
- [Built-in Rules](advanced/validation.md#built-in-validation-rules)
- [Custom Validation](advanced/validation.md#custom-validation)
- [Validation Context](advanced/validation.md#validation-context)

### Performance
- [Connection Pooling](advanced/performance.md#connection-pooling)
- [Batch Operations](advanced/performance.md#batch-operations)
- [Query Optimization](advanced/performance.md#query-optimization)
- [Indexing](advanced/performance.md#indexing)

---

## 💡 Common Use Cases

### User Authentication System
- [Table Schema](core-concepts/table-definition.md#user-management-system)
- [Registration with Validation](advanced/validation.md#user-registration)
- [Transaction Example](core-concepts/transactions.md#user-registration)

### E-Commerce Platform
- [Product Management](core-concepts/table-definition.md#e-commerce-system)
- [Order Processing](core-concepts/transactions.md#e-commerce-order)
- [Batch Operations](advanced/performance.md#batch-operations)

### Blog/CMS System
- [Schema Design](core-concepts/table-definition.md#blog-system)
- [Multi-table JOINs](core-concepts/relationships.md#complex-multi-table-join)
- [Aggregations](core-concepts/queries.md#aggregates)

---

## 🔍 Search by Feature

### By Database Operation
- **CREATE**: [Table Creation](core-concepts/table-definition.md#creating-tables)
- **SELECT**: [Query Guide](core-concepts/queries.md#select-queries)
- **INSERT**: [Insert Operations](core-concepts/queries.md#insert-queries)
- **UPDATE**: [Update Operations](core-concepts/queries.md#update-queries)
- **DELETE**: [Delete Operations](core-concepts/queries.md#delete-queries)
- **JOIN**: [Relationships](core-concepts/relationships.md)

### By Data Validation
- **Email**: [Email Validation](advanced/validation.md#email)
- **String Length**: [Length Validation](advanced/validation.md#string-length)
- **Numeric Range**: [Range Validation](advanced/validation.md#numeric-range)
- **Custom Rules**: [Custom Validation](advanced/validation.md#custom-validation)

### By Performance Topic
- **Batching**: [Batch Operations](advanced/performance.md#batch-operations)
- **Indexing**: [Index Guidelines](advanced/performance.md#indexing)
- **Pooling**: [Connection Pools](advanced/performance.md#connection-pooling)
- **Optimization**: [Query Optimization](advanced/performance.md#query-optimization)

---

## 🎓 Learning Path

### Beginner Path (1-2 hours)
1. Read [Quick Start](getting-started/quick-start.md)
2. Follow [Table Definition](core-concepts/table-definition.md)
3. Try [Basic Queries](core-concepts/queries.md)
4. Run `example-basic`

### Intermediate Path (3-5 hours)
1. Learn [Relationships & JOINs](core-concepts/relationships.md)
2. Study [Transactions](core-concepts/transactions.md)
3. Explore [Validation](advanced/validation.md)
4. Run `example-relationships` and `example-advanced`

### Advanced Path (5+ hours)
1. Master [Performance Optimization](advanced/performance.md)
2. Study complex examples
3. Read [Contributing Guide](../CONTRIBUTING.md)
4. Build your own project

---

## 📦 Examples

All examples are in the [`examples/`](../examples/) directory:

- **[example-basic](../examples/example-basic/)** - CRUD operations
- **[example-relationships](../examples/example-relationships/)** - JOINs
- **[example-aggregates](../examples/example-aggregates/)** - GROUP BY
- **[example-advanced](../examples/example-advanced/)** - Validation & batch ops

Run examples:
```bash
./gradlew :examples:example-basic:run
./gradlew :examples:example-relationships:run
./gradlew :examples:example-aggregates:run
./gradlew :examples:example-advanced:run
```

---

## 🐛 Troubleshooting

### Common Issues
- [Connection Pool Errors](getting-started/quick-start.md#common-issues)
- [Auto-increment Not Working](core-concepts/table-definition.md#auto-increment)
- [JOIN Column Not Found](core-concepts/relationships.md#best-practices)
- [Validation Errors](advanced/validation.md#error-handling)

### Getting Help
- Check [FAQ](#) (Coming Soon)
- Search [GitHub Issues](https://github.com/yourusername/korm-dsl/issues)
- Ask in [Discussions](https://github.com/yourusername/korm-dsl/discussions)

---

## 🔄 Updates & Changes

- **[CHANGELOG](../CHANGELOG.md)** - Version history
- **[Roadmap](#)** - Upcoming features
- **[Breaking Changes](../CHANGELOG.md#breaking-changes)** - Migration guides

---

## 🤝 Contributing

Interested in contributing? See:
- [Contributing Guide](../CONTRIBUTING.md)
- [Code of Conduct](../CONTRIBUTING.md#code-of-conduct)
- [Development Setup](../CONTRIBUTING.md#development-setup)

---

**Last Updated:** December 25, 2024  
**Version:** 0.1.0
