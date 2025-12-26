rootProject.name = "korm-dsl"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":korm-dsl-core")
include(":examples:example-basic")
include(":examples:example-relationships")
include(":examples:example-aggregates")
include(":examples:example-advanced")
include(":examples:example-migrations")
include(":examples:example-monitoring")
include(":examples:example-advanced-queries")
