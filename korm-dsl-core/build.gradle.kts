plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.hikari)
    implementation(libs.postgresql)
    implementation(libs.mysql)
    implementation(libs.sqlite)
    implementation(libs.h2)
    implementation(libs.slf4j.api)

    testImplementation(kotlin("test"))
    testImplementation(libs.logback.classic)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}