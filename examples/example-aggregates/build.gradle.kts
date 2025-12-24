plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":korm-dsl-core"))
    implementation(libs.logback.classic)
}

application {
    mainClass.set("com.korm.examples.aggregates.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}