package com.lemon.mcdevmanagermp.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun appColorScheme(
    seedColor: Color,
    isDark: Boolean,
    useDynamicColor: Boolean,
): ColorScheme {
    val context = LocalContext.current
    if (useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    return if (isDark) seedDarkColorScheme(seedColor) else seedLightColorScheme(seedColor)
}
