plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "com.korm"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}