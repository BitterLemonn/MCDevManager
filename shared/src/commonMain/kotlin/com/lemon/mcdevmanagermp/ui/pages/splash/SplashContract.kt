package com.lemon.mcdevmanagermp.ui.pages.splash

import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class SplashState(
    val isLoading: Boolean = true,
    val countdown: Int = 2
) : IUiState

sealed interface SplashAction : IUiAction {
    data object Tick : SplashAction
    data object Finish : SplashAction
}

sealed interface SplashEffect : IUiEffect {
    data object NavigateToLogin : SplashEffect
    data object NavigateToMain : SplashEffect
}
