package com.lemon.mcdevmanagermp.ui.theme

import android.content.SharedPreferences
import com.lemon.mcdevmanagermp.platform.AndroidLogContext

actual class ThemeRepository actual constructor() {

    private val prefs: SharedPreferences by lazy {
        val ctx = AndroidLogContext.getContext()!!
        ctx.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }

    actual fun getThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    actual fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "mc_dev_theme"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
