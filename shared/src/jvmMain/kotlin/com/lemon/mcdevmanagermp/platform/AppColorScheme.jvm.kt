package com.lemon.mcdevmanagermp.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lemon.mcdevmanagermp.ui.theme.seedDarkColorScheme
import com.lemon.mcdevmanagermp.ui.theme.seedLightColorScheme

@Composable
actual fun appColorScheme(
    seedColor: Color,
    isDark: Boolean,
    useDynamicColor: Boolean,
): ColorScheme {
    return if (isDark) seedDarkColorScheme(seedColor) else seedLightColorScheme(seedColor)
}
