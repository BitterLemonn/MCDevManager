package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.ui.theme.DefaultSeedColorLong
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode
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

    actual fun getSeedColor(): Long {
        return prefs.getLong(KEY_SEED_COLOR, DefaultSeedColorLong)
    }

    actual fun setSeedColor(color: Long) {
        prefs.putLong(KEY_SEED_COLOR, color)
        prefs.flush()
    }

    actual fun getUseDynamicColor(): Boolean = false

    actual fun setUseDynamicColor(use: Boolean) {}

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SEED_COLOR = "seed_color"
    }
}
