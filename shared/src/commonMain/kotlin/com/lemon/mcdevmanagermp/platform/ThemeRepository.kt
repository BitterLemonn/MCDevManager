package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.ui.theme.ThemeMode

/**
 * 平台相关的 ThemeMode 持久化接口。
 * 各平台需通过 actual 提供实现：
 *  - Android: SharedPreferences
 *  - JVM:    java.util.prefs.Preferences
 *  - iOS:    NSUserDefaults
 */
expect class ThemeRepository() {
    fun getThemeMode(): ThemeMode
    fun setThemeMode(mode: ThemeMode)
    fun getSeedColor(): Long
    fun setSeedColor(color: Long)
    fun getUseDynamicColor(): Boolean
    fun setUseDynamicColor(use: Boolean)
}
