// korm-dsl/examples/example-basic/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    // Core module
    implementation(project(":korm-dsl-core"))

    // Database driver
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    // Logging
    implementation(libs.bundles.logging)

    // Testing
    testImplementation(libs.bundles.testing)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
    mainClass.set("com.korm.examples.basic.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}