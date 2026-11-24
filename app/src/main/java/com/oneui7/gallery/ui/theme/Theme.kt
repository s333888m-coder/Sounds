package com.oneui7.gallery.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OneUI7LightBg = Color(0xFFFAF9FE)
val OneUI7LightCard = Color(0xFFEFEEF5)
val OneUI7Blue = Color(0xFF3871E0)

val AmoledBlack = Color(0xFF000000)
val AmoledCard = Color(0xFF141418)
val AmoledElevated = Color(0xFF1F1F26)

private val LightColors = lightColorScheme(
    primary = OneUI7Blue,
    background = OneUI7LightBg,
    surface = OneUI7LightBg,
    surfaceVariant = OneUI7LightCard,
    onBackground = Color(0xFF141418),
    onSurface = Color(0xFF141418)
)

private val AmoledDarkColors = darkColorScheme(
    primary = Color(0xFF6BA0FF),
    background = AmoledBlack,
    surface = AmoledBlack,
    surfaceVariant = AmoledCard,
    surfaceContainerHigh = AmoledElevated,
    onBackground = Color(0xFFF5F5FA),
    onSurface = Color(0xFFF5F5FA)
)

@Composable
fun OneUI7GalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) AmoledDarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
