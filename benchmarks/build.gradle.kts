// korm-dsl/benchmarks/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("me.champeau.jmh") version "0.7.2"
}

dependencies {
    // Core module
    implementation(project(":korm-dsl-core"))

    // Database drivers
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    // Comparison ORMs
    implementation("org.jetbrains.exposed:exposed-core:0.56.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.56.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.56.0")
    implementation("org.hibernate:hibernate-core:6.6.3.Final")
    implementation("org.jooq:jooq:3.19.15")

    // Logging
    implementation(libs.bundles.logging)

    // JMH
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

jmh {
    warmupIterations.set(3)
    iterations.set(5)
    fork.set(2)
    benchmarkMode.set(listOf("thrpt", "avgt"))
    timeUnit.set("ms")
    resultFormat.set("JSON")
}