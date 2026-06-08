package com.lemon.mcdevmanagermp.platform

import androidx.compose.runtime.Composable

/**
 * 根据当前主题模式配置系统状态栏/导航栏外观。
 * Android: 设置状态栏和导航栏图标为浅色/深色
 * 其他平台: no-op
 */
@Composable
expect fun ConfigureSystemBars(isDark: Boolean)
