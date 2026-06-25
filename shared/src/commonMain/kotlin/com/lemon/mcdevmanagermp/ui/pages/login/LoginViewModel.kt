package com.lemon.mcdevmanagermp.ui.pages.login

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.AccountRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.CookieRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.LoginRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.account.SaveAccountUseCase
import com.lemon.mcdevmanagermp.domain.login.LoginUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.main.MainViewModel
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel<LoginState, LoginAction, LoginEffect>(LoginState()) {

    private val loginUseCase = LoginUseCase(
        loginRepository = LoginRepositoryImpl.INSTANCE,
        userRepository = UserRepositoryImpl.INSTANCE,
        cookieRepository = CookieRepositoryImpl.INSTANCE
    )

    private val saveAccountUseCase = SaveAccountUseCase(
        accountRepository = AccountRepositoryImpl.INSTANCE,
        userRepository = UserRepositoryImpl.INSTANCE,
        cookieRepository = CookieRepositoryImpl.INSTANCE
    )

    override fun dispatch(action: LoginAction) {
        when (action) {
            is LoginAction.UpdateEmail -> setState { copy(email = action.email) }
            is LoginAction.UpdatePassword -> setState { copy(password = action.password) }
            is LoginAction.UpdateCookies -> setState { copy(cookies = action.cookies) }
            LoginAction.TogglePasswordVisibility -> setState { copy(isPasswordVisible = !isPasswordVisible) }
            LoginAction.ToggleCookies -> setState { copy(isUsingCookies = !isUsingCookies) }
            LoginAction.Login -> doLogin()
        }
    }

    private fun doLogin() {
        val s = state.value
        if (!s.isUsingCookies) {
            if (s.email.isBlank()) {
                sendEffect(LoginEffect.ShowToast("请输入邮箱"))
                return
            }
            if (s.password.isBlank()) {
                sendEffect(LoginEffect.ShowToast("请输入密码"))
                return
            }
        } else {
            if (s.cookies.isBlank()) {
                sendEffect(LoginEffect.ShowToast("请输入Cookies"))
                return
            }
        }

        setState { copy(isLoading = true) }
        viewModelScope.launch {
            try {
                if (s.isUsingCookies) {
                    loginUseCase(cookies = s.cookies)
                    saveAccountUseCase()
                } else {
                    loginUseCase(email = s.email, password = s.password)
                    saveAccountUseCase(s.email)
                }
                MainViewModel.invalidateAllCache()
                sendEffect(LoginEffect.ShowToast("登录成功"))
                sendEffect(LoginEffect.NavigateTo(Route.Main, Route.Splash))
            } catch (e: Exception) {
                when (e.message) {
                    "413" -> sendEffect(LoginEffect.ShowToast("邮箱或密码错误"))
                    else -> sendEffect(LoginEffect.ShowToast(e.message ?: "登录失败请重试"))
                }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
