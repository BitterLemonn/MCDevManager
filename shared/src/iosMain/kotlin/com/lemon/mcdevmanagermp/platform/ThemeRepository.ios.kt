package com.lemon.mcdevmanagermp.platform

import com.lemon.mcdevmanagermp.ui.theme.DefaultSeedColorLong
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode
import platform.Foundation.NSNumber
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

    actual fun getSeedColor(): Long {
        return (defaults.objectForKey(KEY_SEED_COLOR) as? NSNumber)?.longValue
            ?: DefaultSeedColorLong
    }

    actual fun setSeedColor(color: Long) {
        defaults.setObject(color, forKey = KEY_SEED_COLOR)
        defaults.synchronize()
    }

    actual fun getUseDynamicColor(): Boolean = false

    actual fun setUseDynamicColor(use: Boolean) {}

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SEED_COLOR = "seed_color"
    }
}
