package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SaaSSemanticColors(
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val cardBorder: Color,
    val subtleSurface: Color,
    val textMuted: Color,
)

private val LightSemanticColors = SaaSSemanticColors(
    success = Emerald600,
    successContainer = Emerald50,
    warning = Amber600,
    warningContainer = Amber100,
    danger = Red600,
    dangerContainer = Red100,
    info = Blue600,
    infoContainer = Blue100,
    cardBorder = Slate200,
    subtleSurface = Slate100,
    textMuted = Slate500,
)

private val DarkSemanticColors = SaaSSemanticColors(
    success = Emerald400,
    successContainer = Emerald900,
    warning = Amber500,
    warningContainer = Amber900,
    danger = Red500,
    dangerContainer = Red900,
    info = Color(0xFF60A5FA),
    infoContainer = Blue900,
    cardBorder = Slate800,
    subtleSurface = Slate800,
    textMuted = Slate400,
)

val LocalSaaSColors = staticCompositionLocalOf { LightSemanticColors }

private val LightColorScheme = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald900,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Emerald950,
    tertiary = Amber600,
    onTertiary = Color.White,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate300,
    error = Red600,
    onError = Color.White,
    errorContainer = Red100,
    onErrorContainer = Red900,
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Color(0xFF022C22),
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald100,
    secondary = Emerald400,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = Emerald950,
    onSecondaryContainer = Emerald100,
    tertiary = Amber500,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Amber900,
    onTertiaryContainer = Amber100,
    background = Slate950,
    onBackground = Slate50,
    surface = Color(0xFF111827),
    onSurface = Slate50,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate800,
    outlineVariant = Slate700,
    error = Red500,
    onError = Color.White,
    errorContainer = Red900,
    onErrorContainer = Red100,
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun SmartBusinessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    CompositionLocalProvider(LocalSaaSColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
