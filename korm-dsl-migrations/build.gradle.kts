// korm-dsl/korm-dsl-migrations/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    `maven-publish`
}

dependencies {
    // Core module
    implementation(project(":korm-dsl-core"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    // Migrations
    api(libs.flyway.core)
    api(libs.flyway.database.postgresql)
    api(libs.liquibase.core)

    // Logging
    implementation(libs.bundles.logging)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.testcontainers)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("KORM DSL Migrations")
                description.set("Database migrations module for KORM DSL")
            }
        }
    }
}