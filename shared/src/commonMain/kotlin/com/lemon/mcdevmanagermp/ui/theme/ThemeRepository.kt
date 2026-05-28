package com.lemon.mcdevmanagermp.ui.theme

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
}
