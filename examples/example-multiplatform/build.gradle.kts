// examples/example-multiplatform/build.gradle.kts

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    // Android Target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // JVM Target
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // iOS Targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KormMultiplatform"
            isStatic = true
        }
    }

    sourceSets {
        // Common Source Set
        val commonMain by getting {
            dependencies {
                implementation(project(":korm-dsl-core"))
                implementation(project(":korm-dsl-cache"))

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }

        // Android Source Set
        val androidMain by getting {
            dependencies {
                implementation("org.xerial:sqlite-jdbc:3.45.1.0")
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }

        // JVM Source Set
        val jvmMain by getting {
            dependencies {
                implementation("org.xerial:sqlite-jdbc:3.45.1.0")
                implementation("ch.qos.logback:logback-classic:1.4.14")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
            }
        }

        // iOS Source Set
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // iOS-specific SQLite driver would go here
            }
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.korm.examples.multiplatform"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
