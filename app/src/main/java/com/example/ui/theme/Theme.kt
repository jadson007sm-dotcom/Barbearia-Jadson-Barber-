package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = BlackAbsolute,
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = GoldAccent,
    onSecondary = BlackAbsolute,
    secondaryContainer = Color(0xFF2B220C),
    onSecondaryContainer = GoldBright,
    tertiary = GoldBright,
    onTertiary = BlackAbsolute,
    background = BlackAbsolute,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF383838)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to true for luxury dark aesthetic
    dynamicColor: Boolean = false, // Use brand gold theme instead of device dynamic colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LuxuryDarkColorScheme,
        typography = Typography,
        content = content
    )
}
