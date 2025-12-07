# KORM DSL - Validation Module

Comprehensive validation for KORM DSL entities and fields.

## Features

- **Field Validation**: Validate individual fields with built-in rules
- **Entity Validation**: Validate entire entities
- **Custom Validators**: Create custom validation logic
- **JSR-380 Integration**: Use Bean Validation annotations
- **Async Validation**: Support for asynchronous validation
- **Validation Groups**: Conditional validation based on context
- **Rich Built-in Validators**: Email, URL, phone, credit card, UUID, and more

## Usage

### Basic Entity Validation
```kotlin
data class User(
    val name: String,
    val email: String,
    val age: Int
)

val userValidator = validator<User> {
    field(User::name) {
        notBlank()
        minLength(3)
        maxLength(50)
    }
    
    field(User::email) {
        notBlank()
        email()
    }
    
    field(User::age) {
        positive()
        range(18, 120)
    }
}

val result = userValidator.validate(user)
if (!result.isValid) {
    result.violations.forEach { violation ->
        println("${violation.field}: ${violation.message}")
    }
}
```

### String Validators
```kotlin
validator<User> {
    field(User::email) {
        email()
    }
    
    field(User::phone) {
        phone()
    }
    
    field(User::website) {
        url()
    }
    
    field(User::username) {
        alphanumeric()
        minLength(3)
        maxLength(20)
    }
    
    field(User::zipCode) {
        pattern("^[0-9]{5}$")
    }
}
```

### Number Validators
```kotlin
validator<Product> {
    field(Product::price) {
        positive()
        min(0.01)
        max(10000.0)
    }
    
    field(Product::quantity) {
        range(1, 1000)
    }
}
```

### Collection Validators
```kotlin
validator<Order> {
    field(Order::items) {
        notEmpty()
        size(min = 1, max = 50)
    }
    
    field(Order::tags) {
        unique()
        each { it.isNotBlank() }
    }
}
```

### Custom Validation
```kotlin
validator<User> {
    custom { user ->
        if (user.age < 18 && user.requiresParentalConsent) {
            ValidationResult.invalid(
                ValidationViolation(
                    field = "age",
                    message = "Users under 18 require parental consent"
                )
            )
        } else {
            ValidationResult.valid()
        }
    }
}
```

### Async Validation
```kotlin
val validator = AsyncEntityValidator<User>().apply {
    addValidator { user ->
        val emailExists = database.exists(Users) { 
            Users.email eq user.email 
        }
        
        if (emailExists) {
            ValidationResult.invalid(
                ValidationViolation(
                    field = "email",
                    message = "Email already exists"
                )
            )
        } else {
            ValidationResult.valid()
        }
    }
}

val result = validator.validateAsync(user)
```

### Throwing on Validation Failure
```kotlin
try {
    userValidator.validate(user).orThrow()
    // Proceed with valid user
} catch (e: ValidationException) {
    e.result.violations.forEach { violation ->
        println(violation.message)
    }
}
```

## Built-in Validators

### Common
- `notNull()` - Must not be null
- `notBlank()` - String must not be blank
- `notEmpty()` - String/Collection must not be empty
- `size()` - String/Collection size constraints
- `min()`, `max()`, `range()` - Numeric constraints
- `positive()`, `negative()` - Number sign validation

### String
- `email()` - Valid email format
- `url()` - Valid URL format
- `phone()` - Valid phone number
- `pattern()` - Regex matching
- `alpha()` - Only letters
- `alphanumeric()` - Letters and numbers
- `numeric()` - Only numbers
- `uuid()` - Valid UUID format
- `creditCard()` - Valid credit card (Luhn)

### Collection
- `contains()` - Contains element
- `containsAll()` - Contains all elements
- `unique()` - All elements unique
- `each()` - All elements satisfy condition
- `any()` - At least one satisfies
- `none()` - No element satisfies

## See Also

- [Validation Guide](../../docs/advanced/validation.md)
- [Examples](../../examples/)
