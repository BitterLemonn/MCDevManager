package com.lemon.mcdevmanagermp.ui.theme

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val repository: ThemeRepository = ThemeRepository()
) : ViewModel() {

    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
    }
}
