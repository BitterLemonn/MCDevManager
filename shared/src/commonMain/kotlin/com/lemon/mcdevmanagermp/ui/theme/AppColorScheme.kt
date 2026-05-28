package com.lemon.mcdevmanagermp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
expect fun appColorScheme(
    seedColor: Color = DefaultSeedColor,
    isDark: Boolean,
): ColorScheme
