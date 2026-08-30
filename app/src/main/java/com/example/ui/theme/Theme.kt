package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VibrantRoseLight,
    onPrimary = VibrantRoseContainerDark,
    primaryContainer = VibrantRoseDark,
    onPrimaryContainer = VibrantRoseContainerLight,
    secondary = VibrantVioletLight,
    onSecondary = VibrantVioletContainerDark,
    secondaryContainer = VibrantVioletDark,
    onSecondaryContainer = VibrantVioletContainerLight,
    tertiary = VibrantCyanLight,
    onTertiary = Color(0xFF00354E),
    tertiaryContainer = Color(0xFF004D70),
    onTertiaryContainer = VibrantCyanContainerLight,
    background = VibrantDarkBackground,
    onBackground = VibrantDarkText,
    surface = VibrantDarkSurface,
    onSurface = VibrantDarkText,
    surfaceVariant = VibrantDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = VibrantDarkOutline,
    outlineVariant = VibrantDarkCardBorder,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantRose,
    onPrimary = Color.White,
    primaryContainer = VibrantRoseContainerLight,
    onPrimaryContainer = VibrantRoseContainerDark,
    secondary = VibrantViolet,
    onSecondary = Color.White,
    secondaryContainer = VibrantVioletContainerLight,
    onSecondaryContainer = VibrantVioletContainerDark,
    tertiary = VibrantCyan,
    onTertiary = Color.White,
    tertiaryContainer = VibrantCyanContainerLight,
    onTertiaryContainer = Color(0xFF004D70),
    background = VibrantLightBackground,
    onBackground = VibrantLightText,
    surface = VibrantLightSurface,
    onSurface = VibrantLightText,
    surfaceVariant = VibrantLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454E),
    outline = VibrantLightOutline,
    outlineVariant = VibrantLightCardBorder,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
