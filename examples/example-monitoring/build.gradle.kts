plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.korm.examples.monitoring.MainKt")
}

dependencies {
    implementation(project(":korm-dsl-core"))
    implementation(libs.h2)
    implementation(libs.logback.classic)
}
