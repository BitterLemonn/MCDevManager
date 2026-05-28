package com.lemon.mcdevmanagermp.ui.theme

import java.util.prefs.Preferences

actual class ThemeRepository actual constructor() {

    private val prefs: Preferences by lazy {
        Preferences.userNodeForPackage(ThemeRepository::class.java)
    }

    actual fun getThemeMode(): ThemeMode {
        val name = prefs.get(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    actual fun setThemeMode(mode: ThemeMode) {
        prefs.put(KEY_THEME_MODE, mode.name)
        prefs.flush()
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
