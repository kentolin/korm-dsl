plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":korm-dsl-core"))
    implementation(libs.h2)
    implementation(libs.postgresql)
    implementation(libs.logback.classic)
}

application {
    mainClass.set("com.korm.examples.migration.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
