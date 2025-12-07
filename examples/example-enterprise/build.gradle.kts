// korm-dsl/examples/example-enterprise/build.gradle.kts

plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.korm.examples.enterprise.MainKt")
}

dependencies {
    // KORM modules
    implementation(project(":korm-dsl-core"))
    implementation(project(":korm-dsl-cache"))
    implementation(project(":korm-dsl-migrations"))
    implementation(project(":korm-dsl-validation"))
    implementation(project(":korm-dsl-monitoring"))

    // Ktor server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.jackson)

    // Database
    implementation(libs.postgresql)
    implementation(libs.hikari)

    // Jackson
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.datatype.jsr310)

    // Configuration
    implementation(libs.typesafe.config)

    // Security
    implementation(libs.bcrypt)

    // Monitoring
    implementation(libs.prometheus.client)
    implementation(libs.micrometer.core)
    implementation(libs.micrometer.registry.prometheus)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.slf4j.api)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)
    testImplementation(libs.testcontainers.postgresql)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runWithSampleData") {
    group = "application"
    description = "Run application with sample data"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.korm.examples.enterprise.MainKt")
    environment("LOAD_SAMPLE_DATA", "true")
}
