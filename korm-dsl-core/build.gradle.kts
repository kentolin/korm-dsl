// korm-dsl/korm-dsl-core/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    `maven-publish`
}

kotlin {
    // JVM Target
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs += listOf(
                    "-Xjvm-default=all",
                    "-opt-in=kotlin.RequiresOptIn"
                )
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // Future: Add more targets when needed
    // js(IR) { browser(); nodejs() }
    // androidTarget()
    // iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        // Common source set - platform-agnostic code
        val commonMain by getting {
            dependencies {
                // Kotlin stdlib
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlin.reflect)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Logging API (abstraction)
                api(libs.slf4j.api)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        // JVM source set - JDBC and JVM-specific implementations
        val jvmMain by getting {
            dependencies {
                // HikariCP for connection pooling
                implementation(libs.hikaricp)

                // Logging implementation
                implementation(libs.logback.classic)
            }
        }

        val jvmTest by getting {
            dependencies {
                // JUnit 5
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)

                // Kotest
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.assertions.core)

                // Mockk
                implementation(libs.mockk)

                // Testcontainers
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.testcontainers.mysql)
                implementation(libs.testcontainers.junit5)

                // Database drivers for testing
                implementation(libs.postgresql)
                implementation(libs.mysql)
                implementation(libs.sqlite)
                implementation(libs.h2)
            }
        }
    }
}

// Dokka configuration
tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))

    dokkaSourceSets {
        configureEach {
            displayName.set("KORM DSL Core")
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)

            perPackageOption {
                matchingRegex.set(".*\\.internal.*")
                suppress.set(true)
            }
        }
    }
}

// Publishing configuration
publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("KORM DSL Core")
                description.set("Core module for KORM DSL - Type-safe SQL DSL for Kotlin")
                url.set("https://github.com/padam/korm-dsl")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("padam")
                        name.set("Padam")
                        email.set("padam@example.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/padam/korm-dsl.git")
                    developerConnection.set("scm:git:ssh://github.com/padam/korm-dsl.git")
                    url.set("https://github.com/padam/korm-dsl")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/padam/korm-dsl")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }

        // Local repository for testing
        mavenLocal()
    }
}

// Configure JAR manifest
tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to "KORM DSL Core",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Padam"
        )
    }
}
