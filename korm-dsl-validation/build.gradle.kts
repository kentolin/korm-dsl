// korm-dsl/korm-dsl-validation/build.gradle.kts

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
    implementation(libs.kotlin.reflect)

    // Logging
    implementation(libs.bundles.logging)

    // Testing
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("KORM DSL Validation")
                description.set("Validation module for KORM DSL")
            }
        }
    }
}