// korm-dsl/settings.gradle.kts

rootProject.name = "korm-dsl"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

// Core modules
include(":korm-dsl-core")
include(":korm-dsl-migrations")
include(":korm-dsl-cache")
include(":korm-dsl-validation")
include(":korm-dsl-monitoring")

// Examples
include(":examples:example-basic")
include(":examples:example-relationships")
include(":examples:example-transactions")
include(":examples:example-rest-api")
include(":examples:example-android")
include(":examples:example-multiplatform")
include(":examples:example-enterprise")

// Benchmarks
include(":benchmarks")