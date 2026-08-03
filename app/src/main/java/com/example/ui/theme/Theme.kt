package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AetherDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = MidnightBackground,
    primaryContainer = Color(0x3300F0FF),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPink,
    onSecondary = MidnightBackground,
    secondaryContainer = Color(0x33FF007A),
    onSecondaryContainer = NeonPink,
    tertiary = ElectricViolet,
    onTertiary = TextPrimary,
    background = MidnightBackground,
    onBackground = TextPrimary,
    surface = MidnightDark,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorderColor
)

@Composable
fun AetherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AetherDarkColorScheme,
        typography = Typography,
        content = content
    )
}
