# Relationships & JOINs

Learn how to work with related data using KORM's JOIN capabilities.

---

## Defining Relationships

### One-to-Many Relationship

```kotlin
// Authors table
object Authors : Table("authors") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull()
}

// Books table (many books per author)
object Books : Table("books") {
    val id = int("id").primaryKey().autoIncrement()
    val title = varchar("title", 200).notNull()
    val authorId = int("author_id").notNull().references(Authors.id)
}
```

### Many-to-Many Relationship

```kotlin
// Books table
object Books : Table("books") {
    val id = int("id").primaryKey().autoIncrement()
    val title = varchar("title", 200).notNull()
}

// Categories table
object Categories : Table("categories") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
}

// Junction table
object BookCategories : Table("book_categories") {
    val bookId = int("book_id").notNull().references(Books.id)
    val categoryId = int("category_id").notNull().references(Categories.id)
}
```

---

## INNER JOIN

Returns only rows where there's a match in both tables.

### Basic INNER JOIN

```kotlin
val booksWithAuthors = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .execute { rs ->
        BookWithAuthor(
            bookTitle = rs.getString("title"),
            authorName = rs.getString("name"),
            authorEmail = rs.getString("email")
        )
    }
```

### INNER JOIN with Lambda Style

```kotlin
val booksWithAuthors = Books.select(db)
    .innerJoin(Authors) { books, authors ->
        books.authorId to Authors.id
    }
    .execute { rs ->
        // Mapping logic
    }
```

### INNER JOIN with WHERE

```kotlin
// Get books by UK authors only
val ukBooks = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .where(Authors.country, "UK")
    .execute { rs ->
        rs.getString("title") to rs.getString("name")
    }
```

---

## LEFT JOIN

Returns all rows from the left table, with matching rows from the right table (or NULL).

```kotlin
// Get all authors, including those without books
val authorsWithBooks = Authors.select(db)
    .leftJoinOn(Books, Authors.id, Books.authorId)
    .execute { rs ->
        AuthorWithBooks(
            authorName = rs.getString("name"),
            bookTitle = rs.getString("title") // May be null
        )
    }
```

---

## RIGHT JOIN

Returns all rows from the right table, with matching rows from the left table (or NULL).

```kotlin
// Get all books, including those without assigned authors
val booksWithAuthors = Books.select(db)
    .rightJoinOn(Authors, Books.authorId, Authors.id)
    .execute { rs ->
        // Mapping logic
    }
```

---

## FULL OUTER JOIN

Returns all rows from both tables, with NULLs where there's no match.

```kotlin
val allData = Books.select(db)
    .fullJoinOn(Authors, Books.authorId, Authors.id)
    .execute { rs ->
        // Mapping logic
    }
```

---

## Multiple JOINs

Join more than two tables in a single query.

### Three-Table JOIN

```kotlin
// Books with authors and categories
val booksWithDetails = Books.select(db)
    .select(Books.title, Authors.name, Categories.name)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .innerJoinOn(BookCategories, Books.id, BookCategories.bookId)
    .innerJoinOn(Categories, BookCategories.categoryId, Categories.id)
    .execute { rs ->
        BookDetails(
            title = rs.getString("title"),
            author = rs.getString("name"),
            category = rs.getString("categories_name") // Aliased automatically
        )
    }
```

### Complex Multi-Table JOIN

```kotlin
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val username = varchar("username", 50).notNull()
}

object Posts : Table("posts") {
    val id = int("id").primaryKey().autoIncrement()
    val userId = int("user_id").notNull().references(Users.id)
    val title = varchar("title", 200).notNull()
}

object Comments : Table("comments") {
    val id = int("id").primaryKey().autoIncrement()
    val postId = int("post_id").notNull().references(Posts.id)
    val userId = int("user_id").notNull().references(Users.id)
    val content = text("content").notNull()
}

// Get posts with user info and comment count
val postsWithInfo = Posts.select(db)
    .selectWithAggregate(
        Posts.title,
        Users.username,
        aggregates = listOf(count(alias = "comment_count"))
    )
    .innerJoinOn(Users, Posts.userId, Users.id)
    .leftJoinOn(Comments, Posts.id, Comments.postId)
    .groupBy(Posts.title, Posts.id, Users.username, Users.id)
    .execute { rs ->
        mapOf(
            "title" to rs.getString("title"),
            "author" to rs.getString("username"),
            "comments" to rs.getInt("comment_count")
        )
    }
```

---

## JOINs with Aggregates

### COUNT with JOIN

```kotlin
// Count books per author
val authorBookCounts = Authors.select(db)
    .selectWithAggregate(
        Authors.name,
        aggregates = listOf(count(alias = "book_count"))
    )
    .leftJoinOn(Books, Authors.id, Books.authorId)
    .groupBy(Authors.name, Authors.id)
    .execute { rs ->
        rs.getString("name") to rs.getInt("book_count")
    }
```

### SUM and AVG with JOIN

```kotlin
// Total sales and average price per author
val authorSales = Authors.select(db)
    .selectWithAggregate(
        Authors.name,
        aggregates = listOf(
            sum(Books.price, alias = "total_sales"),
            avg(Books.price, alias = "avg_price"),
            count(alias = "book_count")
        )
    )
    .innerJoinOn(Books, Authors.id, Books.authorId)
    .groupBy(Authors.name, Authors.id)
    .execute { rs ->
        AuthorSales(
            name = rs.getString("name"),
            totalSales = rs.getDouble("total_sales"),
            avgPrice = rs.getDouble("avg_price"),
            bookCount = rs.getInt("book_count")
        )
    }
```

---

## HAVING with JOINs

Filter grouped results after aggregation.

```kotlin
// Find authors with more than 5 books
val prolificAuthors = Authors.select(db)
    .selectWithAggregate(
        Authors.name,
        aggregates = listOf(count(alias = "book_count"))
    )
    .innerJoinOn(Books, Authors.id, Books.authorId)
    .groupBy(Authors.name, Authors.id)
    .having(count(), ">", 5)
    .execute { rs ->
        rs.getString("name") to rs.getInt("book_count")
    }

// Find authors with average book price > $20
val expensiveAuthors = Authors.select(db)
    .selectWithAggregate(
        Authors.name,
        aggregates = listOf(avg(Books.price, alias = "avg_price"))
    )
    .innerJoinOn(Books, Authors.id, Books.authorId)
    .groupBy(Authors.name, Authors.id)
    .having(avg(Books.price), ">", 20.0)
    .execute { rs ->
        rs.getString("name") to rs.getDouble("avg_price")
    }
```

---

## ORDER BY with JOINs

```kotlin
// Books ordered by author name, then book title
val sortedBooks = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .orderBy(Authors.name, asc = true)
    .execute { rs ->
        rs.getString("title") to rs.getString("name")
    }
```

---

## Data Classes for JOINs

### Simple JOIN Result

```kotlin
data class BookWithAuthor(
    val bookId: Int,
    val bookTitle: String,
    val isbn: String,
    val authorId: Int,
    val authorName: String,
    val authorEmail: String
)
```

### Nested Structure

```kotlin
data class Author(
    val id: Int,
    val name: String,
    val email: String
)

data class Book(
    val id: Int,
    val title: String,
    val isbn: String,
    val author: Author
)

// Map the JOIN result
val books = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .execute { rs ->
        Book(
            id = rs.getInt("id"),
            title = rs.getString("title"),
            isbn = rs.getString("isbn"),
            author = Author(
                id = rs.getInt("author_id"),
                name = rs.getString("name"),
                email = rs.getString("email")
            )
        )
    }
```

---

## Common Patterns

### Master-Detail Query

```kotlin
// Get orders with customer information
object Customers : Table("customers") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull()
}

object Orders : Table("orders") {
    val id = int("id").primaryKey().autoIncrement()
    val customerId = int("customer_id").notNull().references(Customers.id)
    val totalAmount = double("total_amount").notNull()
    val orderDate = varchar("order_date", 20).notNull()
}

val ordersWithCustomers = Orders.select(db)
    .innerJoinOn(Customers, Orders.customerId, Customers.id)
    .execute { rs ->
        OrderWithCustomer(
            orderId = rs.getInt("id"),
            orderDate = rs.getString("order_date"),
            totalAmount = rs.getDouble("total_amount"),
            customerName = rs.getString("name"),
            customerEmail = rs.getString("email")
        )
    }
```

### Many-to-Many Query

```kotlin
// Get books with all their categories
val booksWithCategories = Books.select(db)
    .select(Books.title, Categories.name)
    .innerJoinOn(BookCategories, Books.id, BookCategories.bookId)
    .innerJoinOn(Categories, BookCategories.categoryId, Categories.id)
    .execute { rs ->
        rs.getString("title") to rs.getString("categories_name")
    }

// Group by book to get list of categories
val bookCategoryMap = booksWithCategories
    .groupBy({ it.first }, { it.second })
    .mapValues { it.value.toList() }
```

---

## Best Practices

### 1. Always Specify Join Columns

```kotlin
// ✅ GOOD - Explicit columns
.innerJoinOn(Authors, Books.authorId, Authors.id)

// ❌ BAD - Unclear
.innerJoin(Authors) { books, authors ->
    books.authorId to authors.id  // Still works but less clear
}
```

### 2. Select Specific Columns for Clarity

```kotlin
// ✅ GOOD - Explicit selection avoids ambiguity
Books.select(db)
    .select(Books.title, Books.isbn, Authors.name, Authors.email)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
```

### 3. Use Meaningful Data Classes

```kotlin
// ✅ GOOD - Clear data structure
data class BookWithAuthor(
    val bookTitle: String,
    val authorName: String,
    val authorEmail: String
)

// ❌ BAD - Generic map
val results: List<Map<String, Any?>>
```

### 4. Handle NULLs in LEFT JOINs

```kotlin
// ✅ GOOD - Safe null handling
val result = Authors.select(db)
    .leftJoinOn(Books, Authors.id, Books.authorId)
    .execute { rs ->
        Author(
            name = rs.getString("name"),
            bookCount = rs.getString("title")?.let { 1 } ?: 0
        )
    }
```

---

## Performance Considerations

### Index Foreign Keys

Ensure foreign key columns are indexed for better JOIN performance:

```sql
CREATE INDEX idx_books_author_id ON books(author_id);
CREATE INDEX idx_book_categories_book_id ON book_categories(book_id);
CREATE INDEX idx_book_categories_category_id ON book_categories(category_id);
```

### Limit JOINed Results

```kotlin
// Use LIMIT with JOINs
val recentBooks = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .orderBy(Books.publishYear, asc = false)
    .limit(10)
    .execute { rs -> /* ... */ }
```

### Avoid N+1 Queries

```kotlin
// ❌ BAD - N+1 query problem
val books = Books.select(db).execute { /* ... */ }
books.forEach { book ->
    // Separate query for each book!
    val author = Authors.select(db)
        .where(Authors.id, book.authorId)
        .execute { /* ... */ }
}

// ✅ GOOD - Single JOIN query
val booksWithAuthors = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .execute { /* ... */ }
```

---

## Next Steps

- **[Transactions](transactions.md)** - Manage multi-table operations
- **[Validation](../advanced/validation.md)** - Validate related data
- **[Performance](../advanced/performance.md)** - Optimize JOIN queries
