// korm-dsl/benchmarks/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
}

dependencies {
    // Core module
    implementation(project(":korm-dsl-core"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)

    // JMH (Benchmark framework)
    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    // Comparison ORMs
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.hibernate:hibernate-core:6.4.2.Final")
    implementation("org.jooq:jooq:3.19.1")

    // Database drivers
    implementation(libs.postgresql)
    implementation(libs.mysql)
    implementation(libs.sqlite)
    implementation(libs.h2)
    implementation(libs.hikaricp)

    // Logging
    implementation(libs.bundles.logging)
}

benchmark {
    configurations {
        named("main") {
            iterations = 5
            iterationTime = 1
            iterationTimeUnit = "sec"
            warmups = 3
        }
    }

    targets {
        register("main")
    }
}
