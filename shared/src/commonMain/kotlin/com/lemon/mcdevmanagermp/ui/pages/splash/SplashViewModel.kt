package com.lemon.mcdevmanagermp.ui.pages.splash

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel<SplashState, SplashAction, SplashEffect>(SplashState()) {

    init {
        startCountdown()
    }

    override fun dispatch(action: SplashAction) {
        when (action) {
            SplashAction.Tick -> tick()
            SplashAction.Finish -> sendEffect(SplashEffect.NavigateToMain)
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (state.value.countdown > 0) {
                delay(1000L)
                setState { copy(countdown = countdown - 1) }
            }
            setState { copy(isLoading = false) }
            sendEffect(SplashEffect.NavigateToMain)
        }
    }

    private fun tick() {
        setState { copy(countdown = (countdown - 1).coerceAtLeast(0)) }
    }
}
