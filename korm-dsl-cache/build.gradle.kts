// korm-dsl/korm-dsl-cache/build.gradle.kts

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

    // Cache providers
    api(libs.caffeine)
    implementation(libs.jedis)

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
                name.set("KORM DSL Cache")
                description.set("Caching module for KORM DSL")
            }
        }
    }
}