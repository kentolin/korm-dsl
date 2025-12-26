plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.korm.examples.advancedqueries.MainKt")
}

dependencies {
    implementation(project(":korm-dsl-core"))
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.logback.classic)
}
