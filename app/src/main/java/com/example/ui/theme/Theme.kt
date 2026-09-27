package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = Color(0xFFC7F3D0),
    secondary = DarkYellowPrimary,
    onSecondary = Color.Black,
    secondaryContainer = DarkYellowContainer,
    onSecondaryContainer = Color(0xFFFFECC0),
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = BlinkitGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = BlinkitGreenLight,
    onPrimaryContainer = BlinkitGreenDark,
    secondary = BlinkitYellowPrimary,
    onSecondary = Color(0xFF1E2429),
    secondaryContainer = BlinkitYellowLight,
    onSecondaryContainer = BlinkitYellowDark,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightBorder
)

@Composable
fun BlinkCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
