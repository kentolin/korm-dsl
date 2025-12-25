# Data Validation

Comprehensive guide to KORM's built-in validation framework.

---

## Overview

KORM provides a powerful validation framework to ensure data integrity before database operations.

```kotlin
import com.korm.dsl.validation.*

val emailValidator = Validator<String>()
    .addRule(notNull("email"))
    .addRule(email("email"))

val result = emailValidator.validate("user@example.com")
if (result.isValid()) {
    // Proceed with insertion
}
```

---

## Built-in Validation Rules

### Not Null

```kotlin
val validator = Validator<String>()
    .addRule(notNull("username"))

validator.validate(null)       // Invalid
validator.validate("alice")    // Valid
```

### String Length

```kotlin
val validator = Validator<String>()
    .addRule(stringLength("username", min = 3, max = 20))

validator.validate("ab")           // Invalid (too short)
validator.validate("alice")        // Valid
validator.validate("a".repeat(25)) // Invalid (too long)
```

### Email

```kotlin
val validator = Validator<String>()
    .addRule(email("email"))

validator.validate("invalid")              // Invalid
validator.validate("user@example.com")     // Valid
```

### Numeric Range

```kotlin
val ageValidator = Validator<Int>()
    .addRule(range("age", min = 18, max = 120))

ageValidator.validate(15)   // Invalid (too young)
ageValidator.validate(25)   // Valid
ageValidator.validate(150)  // Invalid (too old)
```

### Pattern (Regex)

```kotlin
val phoneValidator = Validator<String>()
    .addRule(pattern(
        "phone",
        "^\\+?[1-9]\\d{1,14}$",
        "Phone must be in E.164 format"
    ))

phoneValidator.validate("123")           // Invalid
phoneValidator.validate("+1234567890")   // Valid
```

### One Of (Enum-like)

```kotlin
val statusValidator = Validator<String>()
    .addRule(oneOf("status", "active", "inactive", "pending"))

statusValidator.validate("deleted")  // Invalid
statusValidator.validate("active")   // Valid
```

### Custom Validation

```kotlin
val passwordValidator = Validator<String>()
    .addRule(custom<String>(
        "password",
        "Password must contain at least one uppercase letter"
    ) { password ->
        password?.any { it.isUpperCase() } ?: false
    })

passwordValidator.validate("lowercase")   // Invalid
passwordValidator.validate("Password1")   // Valid
```

---

## Combining Validators

### Multiple Rules

```kotlin
val userValidator = Validator<String>()
    .addRule(notNull("username"))
    .addRule(stringLength("username", min = 3, max = 20))
    .addRule(pattern("username", "^[a-zA-Z0-9_]+$"))

// All rules must pass
val result = userValidator.validate("alice_123")
```

### Validation Context

Validate multiple fields together:

```kotlin
val ctx = ValidationContext()

ctx.validate("username", username, usernameValidator)
ctx.validate("email", email, emailValidator)
ctx.validate("age", age, ageValidator)

if (!ctx.isValid()) {
    println("Validation errors:")
    ctx.getErrorMessages().forEach { println("  - $it") }
}
```

---

## Real-World Examples

### User Registration

```kotlin
data class RegistrationData(
    val username: String?,
    val email: String?,
    val password: String?,
    val age: Int?
)

fun validateRegistration(data: RegistrationData): ValidationContext {
    val ctx = ValidationContext()
    
    // Username validation
    val usernameValidator = Validator<String>()
        .addRule(notNull("username"))
        .addRule(stringLength("username", min = 3, max = 20))
        .addRule(pattern("username", "^[a-zA-Z0-9_]+$"))
    ctx.validate("username", data.username, usernameValidator)
    
    // Email validation
    val emailValidator = Validator<String>()
        .addRule(notNull("email"))
        .addRule(email("email"))
        .addRule(stringLength("email", max = 255))
    ctx.validate("email", data.email, emailValidator)
    
    // Password validation
    val passwordValidator = Validator<String>()
        .addRule(notNull("password"))
        .addRule(stringLength("password", min = 8, max = 100))
        .addRule(custom<String>("password", "Must contain uppercase") {
            it?.any { c -> c.isUpperCase() } ?: false
        })
        .addRule(custom<String>("password", "Must contain number") {
            it?.any { c -> c.isDigit() } ?: false
        })
    ctx.validate("password", data.password, passwordValidator)
    
    // Age validation
    val ageValidator = Validator<Int>()
        .addRule(notNull("age"))
        .addRule(range("age", min = 18, max = 120))
    ctx.validate("age", data.age, ageValidator)
    
    return ctx
}

// Usage
val data = RegistrationData("alice", "alice@example.com", "Secret123", 25)
val validation = validateRegistration(data)

if (validation.isValid()) {
    // Proceed with database insertion
    Users.insert(db)
        .set(Users.username, data.username!!)
        .set(Users.email, data.email!!)
        .set(Users.age, data.age!!)
        .execute()
} else {
    println("Validation failed:")
    validation.getErrorMessages().forEach { println("  - $it") }
}
```

### Product Data

```kotlin
fun validateProduct(
    name: String?,
    price: Double?,
    stock: Int?,
    category: String?
): ValidationContext {
    val ctx = ValidationContext()
    
    ctx.validate("name", name, Validator<String>()
        .addRule(notNull("name"))
        .addRule(stringLength("name", min = 1, max = 200)))
    
    ctx.validate("price", price, Validator<Double>()
        .addRule(notNull("price"))
        .addRule(range("price", min = 0.01, max = 999999.99)))
    
    ctx.validate("stock", stock, Validator<Int>()
        .addRule(notNull("stock"))
        .addRule(range("stock", min = 0, max = 100000)))
    
    ctx.validate("category", category, Validator<String>()
        .addRule(notNull("category"))
        .addRule(oneOf("category", "Electronics", "Furniture", "Clothing")))
    
    return ctx
}
```

---

## Error Handling

### Checking Validation Results

```kotlin
val result = emailValidator.validate("invalid-email")

when (result) {
    is ValidationResult.Valid -> {
        println("Email is valid")
    }
    is ValidationResult.Invalid -> {
        println("Errors:")
        result.errors.forEach { println("  - $it") }
    }
}
```

### Throwing Exceptions

```kotlin
val ctx = validateRegistration(data)
try {
    ctx.throwIfInvalid()
    // Proceed with insertion
} catch (e: ValidationException) {
    println("Validation failed: ${e.message}")
}
```

### Custom Error Messages

```kotlin
val validator = Validator<String>()
    .addRule(notNull("username", "Please provide a username"))
    .addRule(stringLength(
        "username",
        min = 3,
        max = 20,
        message = "Username must be 3-20 characters"
    ))
```

---

## Integration with Database Operations

### Validate Before Insert

```kotlin
fun createUser(username: String?, email: String?): Int? {
    val ctx = ValidationContext()
    
    ctx.validate("username", username, Validator<String>()
        .addRule(notNull("username"))
        .addRule(stringLength("username", min = 3, max = 20)))
    
    ctx.validate("email", email, Validator<String>()
        .addRule(notNull("email"))
        .addRule(email("email")))
    
    if (!ctx.isValid()) {
        println("Validation failed:")
        ctx.getErrorMessages().forEach { println("  - $it") }
        return null
    }
    
    return Users.insert(db)
        .set(Users.username, username!!)
        .set(Users.email, email!!)
        .execute()
}
```

### Validate in Transactions

```kotlin
fun registerUser(data: RegistrationData): Int {
    val validation = validateRegistration(data)
    validation.throwIfInvalid()  // Throws before transaction starts
    
    return db.transaction { conn ->
        val userId = Users.insert(db)
            .set(Users.username, data.username!!)
            .set(Users.email, data.email!!)
            .execute()
        
        UserProfiles.insert(db)
            .set(UserProfiles.userId, userId)
            .execute()
        
        userId
    }
}
```

---

## Advanced Patterns

### Reusable Validators

```kotlin
object Validators {
    val username = Validator<String>()
        .addRule(notNull("username"))
        .addRule(stringLength("username", min = 3, max = 20))
        .addRule(pattern("username", "^[a-zA-Z0-9_]+$"))
    
    val email = Validator<String>()
        .addRule(notNull("email"))
        .addRule(email("email"))
        .addRule(stringLength("email", max = 255))
    
    val age = Validator<Int>()
        .addRule(notNull("age"))
        .addRule(range("age", min = 18, max = 120))
}

// Usage
val ctx = ValidationContext()
ctx.validate("username", username, Validators.username)
ctx.validate("email", email, Validators.email)
```

### Conditional Validation

```kotlin
fun validateOrder(
    orderType: String,
    deliveryAddress: String?,
    pickupLocation: String?
): ValidationContext {
    val ctx = ValidationContext()
    
    when (orderType) {
        "delivery" -> {
            ctx.validate("deliveryAddress", deliveryAddress, Validator<String>()
                .addRule(notNull("deliveryAddress"))
                .addRule(stringLength("deliveryAddress", min = 10, max = 500)))
        }
        "pickup" -> {
            ctx.validate("pickupLocation", pickupLocation, Validator<String>()
                .addRule(notNull("pickupLocation"))
                .addRule(oneOf("pickupLocation", "Store A", "Store B", "Store C")))
        }
    }
    
    return ctx
}
```

---

## Best Practices

### 1. Validate Early

```kotlin
// ✅ GOOD - Validate before any database operations
val ctx = validateUser(data)
if (!ctx.isValid()) {
    return Response.badRequest(ctx.getErrorMessages())
}
db.transaction { /* ... */ }

// ❌ BAD - Validate inside transaction
db.transaction {
    val ctx = validateUser(data)
    if (!ctx.isValid()) {
        throw ValidationException()  // Unnecessary rollback
    }
}
```

### 2. Provide Clear Error Messages

```kotlin
// ✅ GOOD - Clear, user-friendly messages
.addRule(stringLength(
    "password",
    min = 8,
    message = "Password must be at least 8 characters"
))

// ❌ BAD - Generic messages
.addRule(stringLength("password", min = 8))
// Error: "password must be at least 8 characters" (generic)
```

### 3. Use Validation Contexts for Multiple Fields

```kotlin
// ✅ GOOD - Single context for all fields
val ctx = ValidationContext()
ctx.validate("username", username, usernameValidator)
ctx.validate("email", email, emailValidator)
ctx.validate("age", age, ageValidator)

if (!ctx.isValid()) {
    // Show all errors at once
    return ctx.getErrorMessages()
}

// ❌ BAD - Individual validation
if (!usernameValidator.validate(username).isValid()) return
if (!emailValidator.validate(email).isValid()) return
if (!ageValidator.validate(age).isValid()) return
```

---

## Testing Validators

```kotlin
@Test
fun `email validator should reject invalid emails`() {
    val validator = Validator<String>()
        .addRule(email("email"))
    
    val invalid = listOf(
        "invalid",
        "@example.com",
        "user@",
        "user @example.com"
    )
    
    invalid.forEach { email ->
        val result = validator.validate(email)
        assertFalse(result.isValid(), "Expected $email to be invalid")
    }
}

@Test
fun `email validator should accept valid emails`() {
    val validator = Validator<String>()
        .addRule(email("email"))
    
    val valid = listOf(
        "user@example.com",
        "user.name@example.com",
        "user+tag@example.co.uk"
    )
    
    valid.forEach { email ->
        val result = validator.validate(email)
        assertTrue(result.isValid(), "Expected $email to be valid")
    }
}
```

---

## Next Steps

- **[Performance](performance.md)** - Optimize validation performance
- **[Transactions](../core-concepts/transactions.md)** - Combine validation with transactions
