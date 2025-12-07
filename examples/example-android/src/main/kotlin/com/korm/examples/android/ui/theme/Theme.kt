// examples/example-android/src/main/kotlin/com/korm/examples/android/ui/theme/Theme.kt

package com.korm.examples.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Theme Colors
private val LightPrimary = Color(0xFF6200EE)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFBB86FC)
private val LightSecondary = Color(0xFF03DAC6)
private val LightOnSecondary = Color(0xFF000000)
private val LightError = Color(0xFFB00020)
private val LightBackground = Color(0xFFFFFBFE)
private val LightSurface = Color(0xFFFFFBFE)

// Dark Theme Colors
private val DarkPrimary = Color(0xFFBB86FC)
private val DarkOnPrimary = Color(0xFF000000)
private val DarkPrimaryContainer = Color(0xFF3700B3)
private val DarkSecondary = Color(0xFF03DAC6)
private val DarkOnSecondary = Color(0xFF000000)
private val DarkError = Color(0xFFCF6679)
private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF121212)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    error = LightError,
    background = LightBackground,
    surface = LightSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    error = DarkError,
    background = DarkBackground,
    surface = DarkSurface
)

@Composable
fun KormTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
