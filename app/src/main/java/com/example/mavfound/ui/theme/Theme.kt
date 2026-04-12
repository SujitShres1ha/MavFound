package com.example.mavfound.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MavFoundPrimary,
    secondary = MavFoundSecondary,
    background = MavFoundBackground,
    surface = MavFoundSurface
)

@Composable
fun MavFoundTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
