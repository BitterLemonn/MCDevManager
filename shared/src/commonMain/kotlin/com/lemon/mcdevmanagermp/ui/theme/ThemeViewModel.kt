package com.lemon.mcdevmanagermp.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val repository: ThemeRepository = ThemeRepository()
) : ViewModel() {

    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _seedColor = MutableStateFlow(Color(repository.getSeedColor()))
    val seedColor: StateFlow<Color> = _seedColor.asStateFlow()

    private val _useDynamicColor = MutableStateFlow(repository.getUseDynamicColor())
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
    }

    fun setSeedColor(color: Color) {
        _seedColor.value = color
        repository.setSeedColor(colorToLong(color))
    }

    fun setUseDynamicColor(use: Boolean) {
        _useDynamicColor.value = use
        repository.setUseDynamicColor(use)
    }
}

private fun colorToLong(color: Color): Long {
    val a = (color.alpha * 255 + 0.5f).toInt().toLong()
    val r = (color.red * 255 + 0.5f).toInt().toLong()
    val g = (color.green * 255 + 0.5f).toInt().toLong()
    val b = (color.blue * 255 + 0.5f).toInt().toLong()
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

val LocalThemeViewModel = compositionLocalOf<ThemeViewModel> {
    error("No ThemeViewModel provided")
}
