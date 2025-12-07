# examples/example-android/proguard-rules.pro

# Keep KORM classes
-keep class com.korm.** { *; }
-keepclassmembers class com.korm.** { *; }

# Keep SQLite JDBC driver
-keep class org.sqlite.** { *; }

# Keep data classes
-keep class com.korm.examples.android.data.** { *; }
-keepclassmembers class com.korm.examples.android.data.** { *; }

# Kotlin
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# Compose
-keep class androidx.compose.** { *; }
