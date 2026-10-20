package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette Tokens
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Brand Colors
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueHover = Color(0xFF1D4ED8)
val EmeraldSuccess = Color(0xFF059669)
val EmeraldLight = Color(0xFFD1FAE5)
val RoseExpense = Color(0xFFE11D48)
val RoseLight = Color(0xFFFFE4E6)
val AmberWarning = Color(0xFFD97706)
val AmberLight = Color(0xFFFEF3C7)
val PurpleAccent = Color(0xFF7C3AED)
val PurpleLight = Color(0xFFEDE9FE)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF10B981),
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFF59E0B),
    background = Color(0xFF0B1120),
    surface = Color(0xFF151F32),
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEFF6FF),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = EmeraldSuccess,
    onSecondary = Color.White,
    secondaryContainer = EmeraldLight,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = AmberWarning,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    outlineVariant = Color(0xFFF1F5F9)
)

@Composable
fun OrrExpencyTheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
