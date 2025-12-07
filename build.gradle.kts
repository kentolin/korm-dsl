// korm-dsl/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    group = "com.korm"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}