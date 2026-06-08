package com.lemon.mcdevmanagermp.platform

import android.content.Context
import androidx.core.content.edit
import com.lemon.mcdevmanagermp.ui.theme.DefaultSeedColorLong
import com.lemon.mcdevmanagermp.ui.theme.ThemeMode
import java.lang.ref.WeakReference

actual class ThemeRepository actual constructor() {

    private val prefs by lazy {
        val context = themeContext
            ?: throw IllegalStateException("请先调用 ThemeRepository.init(context) 初始化")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    actual fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
    }

    actual fun getSeedColor(): Long {
        return prefs.getLong(KEY_SEED_COLOR, DefaultSeedColorLong)
    }

    actual fun setSeedColor(color: Long) {
        prefs.edit { putLong(KEY_SEED_COLOR, color) }
    }

    actual fun getUseDynamicColor(): Boolean {
        return prefs.getBoolean(KEY_USE_DYNAMIC_COLOR, false)
    }

    actual fun setUseDynamicColor(use: Boolean) {
        prefs.edit { putBoolean(KEY_USE_DYNAMIC_COLOR, use) }
    }

    companion object {
        private const val PREFS_NAME = "mc_dev_theme"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SEED_COLOR = "seed_color"
        private const val KEY_USE_DYNAMIC_COLOR = "use_dynamic_color"

        private var contextRef: WeakReference<Context>? = null

        private val themeContext: Context?
            get() = contextRef?.get()

        fun init(context: Context) {
            contextRef = WeakReference(context.applicationContext)
        }
    }
}
