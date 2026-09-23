package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SocialGreen,
    onPrimary = Color.White,
    primaryContainer = SocialGreenUltraLight,
    onPrimaryContainer = SocialGreenDark,
    secondary = SocialWarm,
    onSecondary = Color.White,
    secondaryContainer = SocialWarmLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SocialTeal,
    background = Color(0xFFFBF9F4), // Keep warm cream background
    surface = Color.White,
    surfaceVariant = Color(0xFFF5EFE6),
    onBackground = Color(0xFF0F172A), // Always high-contrast dark text
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = SocialGreen,
    onPrimary = Color.White,
    primaryContainer = SocialGreenUltraLight,
    onPrimaryContainer = SocialGreenDark,
    secondary = SocialWarm,
    onSecondary = Color.White,
    secondaryContainer = SocialWarmLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SocialTeal,
    tertiaryContainer = SocialTealLight,
    background = Color(0xFFFBF9F4),
    surface = Color.White,
    surfaceVariant = Color(0xFFF5EFE6),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Keep consistent creamy professional theme across all devices
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
