package com.velocoach.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OrangePrimary = Color(0xFFE8501F)
private val OrangeDark = Color(0xFFFF7A50)
private val Charcoal = Color(0xFF1C1B1F)

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = Color(0xFF00696E),
    tertiary = Color(0xFFB35C00)
)

private val DarkColors = darkColorScheme(
    primary = OrangeDark,
    onPrimary = Color(0xFF3A1200),
    secondary = Color(0xFF4CD9E1),
    tertiary = Color(0xFFFFB874),
    background = Charcoal
)

@Composable
fun VeloCoachTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
