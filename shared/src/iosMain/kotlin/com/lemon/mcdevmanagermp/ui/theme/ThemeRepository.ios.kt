package com.lemon.mcdevmanagermp.ui.theme

import platform.Foundation.NSUserDefaults

actual class ThemeRepository actual constructor() {

    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun getThemeMode(): ThemeMode {
        val name = defaults.stringForKey(KEY_THEME_MODE) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    actual fun setThemeMode(mode: ThemeMode) {
        defaults.setObject(mode.name, forKey = KEY_THEME_MODE)
        defaults.synchronize()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
