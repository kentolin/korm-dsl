// korm-dsl/examples/example-rest-api/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    // Core modules
    implementation(project(":korm-dsl-core"))
    implementation(project(":korm-dsl-cache"))
    implementation(project(":korm-dsl-validation"))

    // Ktor
    implementation(libs.bundles.ktor.server)

    // Database
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    // Logging
    implementation(libs.bundles.logging)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.ktor.server.tests)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
    mainClass.set("com.korm.examples.restapi.ApplicationKt")
}