package com.lemon.mcdevmanagermp.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun ConfigureSystemBars(isDark: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            // isAppearanceLightStatusBars = true 表示状态栏图标使用深色（适合浅色背景）
            // isAppearanceLightStatusBars = false 表示状态栏图标使用浅色（适合深色背景）
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }
}
