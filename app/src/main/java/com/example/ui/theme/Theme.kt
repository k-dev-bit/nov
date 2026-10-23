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
    primary = NovaCyan,
    onPrimary = CyberBlack,
    primaryContainer = Color(0xFF003847),
    onPrimaryContainer = NovaCyan,
    secondary = NovaPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3B1561),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = NovaBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0B2D6B),
    onTertiaryContainer = Color(0xFFBFDBFE),
    background = CyberBlack,
    onBackground = TextPrimaryDark,
    surface = CyberDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberBorder,
    outlineVariant = Color(0xFF1B2433),
    error = NovaRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008394),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F3FA),
    onPrimaryContainer = Color(0xFF00353D),
    secondary = Color(0xFF7B2CBF),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = NovaBlue,
    onTertiary = Color.White,
    background = CyberLightBg,
    onBackground = TextPrimaryLight,
    surface = CyberLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CyberLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = CyberLightBorder,
    error = NovaRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep futuristic cyber brand identity consistent
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
