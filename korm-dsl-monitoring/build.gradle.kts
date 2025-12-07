// korm-dsl/korm-dsl-monitoring/build.gradle.kts

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

    // Monitoring
    api(libs.bundles.monitoring)

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
                name.set("KORM DSL Monitoring")
                description.set("Monitoring and metrics module for KORM DSL")
            }
        }
    }
}