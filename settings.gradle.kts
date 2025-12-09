// korm-dsl/settings.gradle.kts

rootProject.name = "korm-dsl"

// Enable version catalogs
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Core modules
include(":korm-dsl-core")
include(":korm-dsl-cache")
include(":korm-dsl-migrations")
include(":korm-dsl-validation")
include(":korm-dsl-monitoring")

// Examples
include(":examples:example-basic")
include(":examples:example-relationships")
include(":examples:example-rest-api")
include(":examples:example-transactions")
include(":examples:example-enterprise")
include(":examples:example-android")
include(":examples:example-multiplatform")

// Benchmarks
include(":benchmarks")

// Set project directories
project(":korm-dsl-core").projectDir = file("korm-dsl-core")
project(":korm-dsl-cache").projectDir = file("korm-dsl-cache")
project(":korm-dsl-migrations").projectDir = file("korm-dsl-migrations")
project(":korm-dsl-validation").projectDir = file("korm-dsl-validation")
project(":korm-dsl-monitoring").projectDir = file("korm-dsl-monitoring")

project(":examples:example-basic").projectDir = file("examples/example-basic")
project(":examples:example-relationships").projectDir = file("examples/example-relationships")
project(":examples:example-rest-api").projectDir = file("examples/example-rest-api")
project(":examples:example-transactions").projectDir = file("examples/example-transactions")
project(":examples:example-enterprise").projectDir = file("examples/example-enterprise")
project(":examples:example-android").projectDir = file("examples/example-android")
project(":examples:example-multiplatform").projectDir = file("examples/example-multiplatform")

project(":benchmarks").projectDir = file("benchmarks")
