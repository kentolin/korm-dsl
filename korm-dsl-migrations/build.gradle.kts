// korm-dsl/korm-dsl-migrations/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    `maven-publish`
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs += listOf("-Xjvm-default=all")
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Core module
                api(project(":korm-dsl-core"))

                // Kotlin
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)

                // Logging
                api(libs.slf4j.api)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                // Flyway
                api(libs.flyway.core)
                api(libs.flyway.database.postgresql)

                // Liquibase
                api(libs.liquibase.core)

                // Logging implementation
                implementation(libs.logback.classic)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.assertions.core)
                implementation(libs.mockk)
                implementation(libs.testcontainers.core)
                implementation(libs.testcontainers.postgresql)
                implementation(libs.postgresql)
                implementation(libs.h2)
            }
        }
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("KORM DSL Migrations")
                description.set("Database migrations module for KORM DSL with Flyway and Liquibase integration")
                url.set("https://github.com/padam/korm-dsl")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}
