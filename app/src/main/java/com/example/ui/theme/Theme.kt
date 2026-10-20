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
    primary = EmeraldNeon,
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005238),
    onPrimaryContainer = Color(0xFF86F8CE),
    secondary = CyanNeon,
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF004D63),
    onSecondaryContainer = Color(0xFFBDEAFE),
    tertiary = VioletNeon,
    onTertiary = Color(0xFF380066),
    tertiaryContainer = Color(0xFF561D8A),
    onTertiaryContainer = Color(0xFFEADBFF),
    background = CyberBackground,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F8E5),
    onPrimaryContainer = Color(0xFF002113),
    secondary = CyanDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3EFFF),
    onSecondaryContainer = Color(0xFF001F2A),
    tertiary = Color(0xFF7C3AED),
    onTertiary = Color.White,
    background = CleanBackground,
    onBackground = CleanTextPrimary,
    surface = CleanSurface,
    onSurface = CleanTextPrimary,
    surfaceVariant = CleanSurfaceVariant,
    onSurfaceVariant = CleanTextSecondary,
    outline = CleanCardBorder
)

@Composable
fun TajSendTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark theme for TajSend brand
    dynamicColor: Boolean = false, // Keep distinct TajSend branding colors consistent
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TajSendTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
