package com.example.refood.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightOnAccent,
    primaryContainer = LightAccentSoft,
    onPrimaryContainer = LightInk,
    secondary = LightDeal,
    onSecondary = Color.White,
    secondaryContainer = LightAccentSoft,
    onSecondaryContainer = LightDeal,
    tertiary = LightInkSoft,
    onTertiary = Color.White,
    error = LightError,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = LightPaper,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    surfaceVariant = LightSurfaceSunken,
    onSurfaceVariant = LightInkSoft,
    outline = LightLine,
    outlineVariant = LightLine,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkOnAccent,
    primaryContainer = DarkAccentSoft,
    onPrimaryContainer = DarkInk,
    secondary = DarkDeal,
    onSecondary = Color(0xFF2A1B0C),
    secondaryContainer = DarkAccentSoft,
    onSecondaryContainer = DarkDeal,
    tertiary = DarkInkSoft,
    onTertiary = Color(0xFF08150E),
    error = DarkError,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkPaper,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = DarkSurfaceSunken,
    onSurfaceVariant = DarkInkSoft,
    outline = DarkLine,
    outlineVariant = DarkLine,
)

@Composable
fun ReFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ReFoodShapes,
        content = content
    )
}
