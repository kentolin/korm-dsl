// korm-dsl/korm-dsl-core/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    `maven-publish`
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)

    // Database
    implementation(libs.hikaricp)

    // Logging
    implementation(libs.bundles.logging)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.postgresql)
    testImplementation(libs.mysql)
    testImplementation(libs.sqlite)
    testImplementation(libs.h2)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("KORM DSL Core")
                description.set("Core module for KORM DSL - Type-safe SQL DSL for Kotlin")
                url.set("https://github.com/yourusername/korm-dsl")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("yourusername")
                        name.set("Your Name")
                        email.set("your.email@example.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/yourusername/korm-dsl.git")
                    developerConnection.set("scm:git:ssh://github.com/yourusername/korm-dsl.git")
                    url.set("https://github.com/yourusername/korm-dsl")
                }
            }
        }
    }
}