package com.lemon.mcdevmanagermp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun appColorScheme(
    seedColor: Color,
    isDark: Boolean,
): ColorScheme {
    return if (isDark) seedDarkColorScheme(seedColor) else seedLightColorScheme(seedColor)
}
