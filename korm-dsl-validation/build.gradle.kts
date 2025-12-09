// korm-dsl/korm-dsl-validation/build.gradle.kts

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
                implementation(libs.kotlin.reflect)

                // Logging
                api(libs.slf4j.api)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmMain by getting {
            dependencies {
                // Logging implementation
                implementation(libs.logback.classic)

                // Optional: JSR-380 Bean Validation
                compileOnly("javax.validation:validation-api:2.0.1.Final")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.assertions.core)
                implementation(libs.mockk)
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
                name.set("KORM DSL Validation")
                description.set("Validation module for KORM DSL with support for custom rules and JSR-380")
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
