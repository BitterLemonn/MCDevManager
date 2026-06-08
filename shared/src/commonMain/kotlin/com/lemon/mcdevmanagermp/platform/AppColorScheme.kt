package com.lemon.mcdevmanagermp.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lemon.mcdevmanagermp.ui.theme.DefaultSeedColor

@Composable
expect fun appColorScheme(
    seedColor: Color = DefaultSeedColor,
    isDark: Boolean,
    useDynamicColor: Boolean = false,
): ColorScheme
