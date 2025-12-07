# KORM Android Example

A complete Android application demonstrating KORM-DSL usage with Jetpack Compose and modern Android architecture.

## Features

- ✅ **KORM-DSL Integration**: SQLite database with KORM ORM
- ✅ **Jetpack Compose UI**: Modern declarative UI
- ✅ **MVVM Architecture**: Clean separation of concerns
- ✅ **Repository Pattern**: Data abstraction layer
- ✅ **Coroutines**: Async database operations
- ✅ **LiveData**: Reactive data updates
- ✅ **Navigation**: Multi-screen navigation
- ✅ **Material 3**: Material Design 3 components

## KORM-DSL Features Demonstrated

### 1. Database Setup
```kotlin
val database = Database.connect(
    url = "jdbc:sqlite:$dbPath",
    driver = "org.sqlite.JDBC"
)
database.createTables(Users)
```

### 2. CRUD Operations

**Create:**
```kotlin
val userId = database.transaction {
    val query = insertInto(Users) {
        it[Users.name] = name
        it[Users.email] = email
        it[Users.age] = age
    }
    insert(query)
}
```

**Read:**
```kotlin
val users = database.transaction {
    val query = from(Users).orderBy(Users.createdAt, "DESC")
    select(query, ::mapUser)
}
```

**Update:**
```kotlin
database.transaction {
    val query = update(Users) {
        it[Users.name] = newName
        it[Users.updatedAt] = System.currentTimeMillis()
    }.where(Users.id eq userId)
    update(query)
}
```

**Delete:**
```kotlin
database.transaction {
    val query = deleteFrom(Users) {
        where(Users.id eq userId)
    }
    delete(query)
}
```

### 3. Advanced Queries

**Search:**
```kotlin
val query = from(Users)
    .where(Users.name like "%$searchTerm%")
    .orderBy(Users.name)
```

**Filter:**
```kotlin
val query = from(Users)
    .where((Users.age gte minAge) and (Users.age lte maxAge))
    .orderBy(Users.age)
```

**Pagination:**
```kotlin
val query = from(Users)
    .orderBy(Users.createdAt, "DESC")
    .limit(pageSize)
    .offset((page - 1) * pageSize)
```

## Architecture
```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
│  ┌──────────┐  ┌──────────────┐   │
│  │  Screen  │  │  ViewModel   │   │
│  └──────────┘  └──────────────┘   │
├─────────────────────────────────────┤
│       Domain Layer (Repository)     │
│  ┌──────────────────────────────┐  │
│  │    UserRepository            │  │
│  └──────────────────────────────┘  │
├─────────────────────────────────────┤
│       Data Layer (DAO)              │
│  ┌──────────────────────────────┐  │
│  │    UserDao (KORM-DSL)        │  │
│  └──────────────────────────────┘  │
├─────────────────────────────────────┤
│       SQLite Database               │
└─────────────────────────────────────┘
```

## Screens

### 1. User List Screen
- Display all users from database
- Search by name
- Filter by status (active/inactive)
- Filter by age range
- View user statistics
- Navigate to user details
- Add new user

### 2. User Detail Screen
- View user information
- Edit user details
- Toggle active status
- Delete user

### 3. Add User Screen
- Form validation
- Create new user
- Error handling

## Building and Running

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 24+
- Kotlin 1.9+

### Build
```bash
./gradlew :examples:example-android:assembleDebug
```

### Install
```bash
./gradlew :examples:example-android:installDebug
```

### Run
```bash
./gradlew :examples:example-android:run
```

## Project Structure
```
example-android/
├── src/main/
│   ├── kotlin/com/korm/examples/android/
│   │   ├── MainActivity.kt                 # Entry point
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── dao/
│   │   │   │   │   └── UserDao.kt         # KORM-DSL operations
│   │   │   │   └── entities/
│   │   │   │       ├── UserEntity.kt      # Data model
│   │   │   │       └── Tables.kt          # Table definitions
│   │   │   └── repository/
│   │   │       └── UserRepository.kt      # Data abstraction
│   │   ├── di/
│   │   │   ├── DatabaseModule.kt          # DB initialization
│   │   │   └── RepositoryModule.kt        # Dependency injection
│   │   └── ui/
│   │       ├── screens/
│   │       │   ├── UserListScreen.kt      # List screen
│   │       │   ├── UserDetailScreen.kt    # Detail screen
│   │       │   └── AddUserScreen.kt       # Create screen
│   │       ├── theme/
│   │       │   ├── Theme.kt               # App theme
│   │       │   └── Type.kt                # Typography
│   │       └── viewmodels/
│   │           └── UserViewModel.kt       # Business logic
│   ├── res/                               # Resources
│   └── AndroidManifest.xml                # App manifest
└── build.gradle.kts                       # Build configuration
```

## Key Classes

### UserDao
Handles all database operations using KORM-DSL:
- `insert()` - Create user
- `findById()` - Get user by ID
- `findAll()` - Get all users
- `update()` - Update user
- `delete()` - Delete user
- `searchByName()` - Search users
- `findByAgeRange()` - Filter by age

### UserRepository
Provides clean API for ViewModels:
- Business logic validation
- Error handling
- Data transformation

### UserViewModel
Manages UI state and business logic:
- LiveData for reactive updates
- Coroutines for async operations
- State management

## Testing

Run unit tests:
```bash
./gradlew :examples:example-android:testDebugUnitTest
```

Run instrumented tests:
```bash
./gradlew :examples:example-android:connectedDebugAndroidTest
```

## Performance

- **Database Size**: ~50KB (minimal overhead)
- **Query Performance**: Sub-millisecond for simple queries
- **Memory Usage**: Efficient with caching
- **APK Size**: ~2MB (with ProGuard)

## Best Practices

1. **Always use transactions** for data consistency
2. **Run database operations on IO dispatcher** to avoid blocking UI
3. **Use caching** for frequently accessed data
4. **Implement proper error handling**
5. **Validate input** before database operations
6. **Close database** in onDestroy

## License

MIT License - see [LICENSE](../../LICENSE)
