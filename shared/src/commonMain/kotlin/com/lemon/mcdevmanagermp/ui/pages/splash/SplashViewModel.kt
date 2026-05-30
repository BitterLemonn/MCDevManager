package com.lemon.mcdevmanagermp.ui.pages.splash

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.AccountRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.account.AutoLoginUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : BaseViewModel<SplashState, SplashAction, SplashEffect>(SplashState()) {

    private val autoLoginUseCase = AutoLoginUseCase(
        accountRepository = AccountRepositoryImpl.INSTANCE,
        userRepository = UserRepositoryImpl.INSTANCE
    )

    private val autoLoginSuccess = MutableStateFlow(false)
    private val autoLoginChecked = MutableStateFlow(false)

    init {
        startCountdown()
        checkAutoLogin()
    }

    override fun dispatch(action: SplashAction) {
        when (action) {
            SplashAction.Tick -> tick()
            SplashAction.Finish -> tryNavigate()
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (state.value.countdown > 0) {
                delay(1000L)
                setState { copy(countdown = countdown - 1) }
            }
            setState { copy(isLoading = false) }
            tryNavigate()
        }
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            autoLoginSuccess.value = autoLoginUseCase()
            autoLoginChecked.value = true
            tryNavigate()
        }
    }

    private fun tryNavigate() {
        if (state.value.countdown > 0 || !autoLoginChecked.value) return
        if (autoLoginSuccess.value) {
            sendEffect(SplashEffect.NavigateToMain)
        } else {
            sendEffect(SplashEffect.NavigateToLogin)
        }
    }

    private fun tick() {
        setState { copy(countdown = (countdown - 1).coerceAtLeast(0)) }
    }
}
