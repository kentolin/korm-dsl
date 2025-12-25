# Contributing to KORM DSL

Thank you for your interest in contributing to KORM DSL! This document provides guidelines for contributing to the project.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inspiring community for all. Please be respectful and constructive in all interactions.

### Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing viewpoints
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

---

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates.

**Good bug reports include:**

- Clear, descriptive title
- Exact steps to reproduce the problem
- Expected vs actual behavior
- Code samples demonstrating the issue
- KORM version, Kotlin version, database type
- Stack traces if applicable

**Bug Report Template:**

```markdown
**Description**
A clear description of the bug

**Steps to Reproduce**
1. Create table with...
2. Execute query...
3. See error

**Expected Behavior**
What you expected to happen

**Actual Behavior**
What actually happened

**Environment**
- KORM Version: 0.1.0
- Kotlin Version: 2.1.0
- Database: PostgreSQL 15
- OS: macOS 14.0

**Additional Context**
Stack traces, logs, etc.
```

### Suggesting Enhancements

Enhancement suggestions are welcome! Please include:

- Clear use case
- Why this would be useful
- Example API (if applicable)
- Potential implementation approach

### Pull Requests

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Add tests for your changes
5. Ensure all tests pass
6. Update documentation
7. Commit your changes (`git commit -m 'Add amazing feature'`)
8. Push to the branch (`git push origin feature/amazing-feature`)
9. Open a Pull Request

---

## Development Setup

### Prerequisites

- JDK 21 or higher
- Kotlin 2.1.0 or higher
- Gradle 8.14 or higher
- PostgreSQL (for integration tests)

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/korm-dsl.git
   cd korm-dsl
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run tests**
   ```bash
   ./gradlew test
   ```

4. **Run examples**
   ```bash
   ./gradlew :examples:example-basic:run
   ```

### Project Structure

```
korm-dsl/
├── korm-dsl-core/          # Core library
│   ├── core/               # Connection, Database
│   ├── dialect/            # SQL dialects
│   ├── schema/             # Table definitions
│   ├── query/              # Query builders
│   ├── expressions/        # Aggregates, expressions
│   └── validation/         # Validation framework
│
├── examples/               # Example projects
├── docs/                   # Documentation
└── benchmarks/             # Performance benchmarks
```

---

## Pull Request Process

### Before Submitting

- [ ] Code follows project style guidelines
- [ ] All tests pass locally
- [ ] New tests added for new functionality
- [ ] Documentation updated
- [ ] Commits are clear and descriptive
- [ ] Branch is up to date with main

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe testing performed

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
- [ ] All tests passing
```

### Review Process

1. Automated checks must pass (CI/CD)
2. At least one maintainer approval required
3. All comments must be addressed
4. Squash and merge when approved

---

## Coding Standards

### Kotlin Style

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

**Key points:**

```kotlin
// ✅ GOOD - Clear, descriptive names
fun getUsersByEmail(email: String): List<User>

// ❌ BAD - Unclear abbreviations
fun getUsrByEml(e: String): List<User>

// ✅ GOOD - Proper indentation (4 spaces)
class SelectQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    // ...
}

// ✅ GOOD - Clear class structure
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
}
```

### Documentation

All public APIs must be documented:

```kotlin
/**
 * Selects rows from the table with optional filtering
 *
 * @param db Database instance
 * @return SelectQuery builder for method chaining
 */
fun <T : Table> T.select(db: Database): SelectQuery<T>
```

### Error Handling

- Use exceptions for exceptional cases
- Provide clear error messages
- Include relevant context in exceptions

```kotlin
// ✅ GOOD - Clear error message
throw IllegalArgumentException(
    "Invalid batch size: $size. Must be between 1 and 1000"
)

// ❌ BAD - Unclear message
throw Exception("Invalid size")
```

---

## Testing Guidelines

### Test Structure

```kotlin
class SelectQueryTest {
    private lateinit var db: Database
    
    @BeforeEach
    fun setup() {
        val pool = ConnectionPool.create(
            url = "jdbc:h2:mem:test",
            driver = "org.h2.Driver"
        )
        db = Database(H2Dialect, pool)
        Users.create(db)
    }
    
    @Test
    fun `select should return all users when no filters applied`() {
        // Given
        Users.insert(db).set(Users.name, "Alice").execute()
        Users.insert(db).set(Users.name, "Bob").execute()
        
        // When
        val users = Users.select(db).execute { rs ->
            rs.getString("name")
        }
        
        // Then
        assertEquals(2, users.size)
        assertTrue(users.contains("Alice"))
        assertTrue(users.contains("Bob"))
    }
}
```

### Test Coverage

- Aim for >80% code coverage
- Test happy paths and edge cases
- Include integration tests for database operations
- Test all supported databases where applicable

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :korm-dsl-core:test

# With coverage
./gradlew test jacocoTestReport
```

---

## Documentation

### Types of Documentation

1. **Code Comments** - Explain complex logic
2. **API Documentation** - KDoc for public APIs
3. **User Guides** - In `docs/` directory
4. **Examples** - Working code in `examples/`
5. **README** - Quick start guide

### Documentation Standards

- Use clear, concise language
- Provide code examples
- Include common use cases
- Document breaking changes

### Updating Documentation

When adding features:

1. Update relevant markdown files in `docs/`
2. Add code examples
3. Update README if needed
4. Add example project if applicable

---

## Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

### Creating a Release

1. Update version in `build.gradle.kts`
2. Update CHANGELOG.md
3. Create git tag
4. Build and publish artifacts
5. Create GitHub release

---

## Getting Help

- **Discussions**: For questions and ideas
- **Issues**: For bugs and feature requests
- **Discord**: [Community chat](#) (coming soon)

---

## Recognition

Contributors are recognized in:

- CHANGELOG.md
- GitHub contributors page
- Release notes

Thank you for contributing to KORM DSL! 🎉
