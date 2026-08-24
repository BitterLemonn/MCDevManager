package com.lemon.mcdevmanagermp.ui.pages.login

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.repository.AccountRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.CookieRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.LoginRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.account.Account
import com.lemon.mcdevmanagermp.domain.account.SaveAccountUseCase
import com.lemon.mcdevmanagermp.domain.login.LoginUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.ui.pages.main.MainViewModel
import kotlinx.coroutines.launch

class LoginViewModel : BaseViewModel<LoginState, LoginAction, LoginEffect>(LoginState()) {

    private val accountRepository = AccountRepositoryImpl.INSTANCE

    private val loginUseCase = LoginUseCase(
        loginRepository = LoginRepositoryImpl.INSTANCE,
        userRepository = UserRepositoryImpl.INSTANCE,
        cookieRepository = CookieRepositoryImpl.INSTANCE
    )

    private val saveAccountUseCase = SaveAccountUseCase(
        accountRepository = accountRepository,
        userRepository = UserRepositoryImpl.INSTANCE,
        cookieRepository = CookieRepositoryImpl.INSTANCE
    )

    init {
        loadSavedAccounts()
    }

    private fun loadSavedAccounts() {
        viewModelScope.launch {
            val accounts = accountRepository.getAllAccounts()
            val savedWithEmail = accounts.filter { it.email.isNotBlank() }
            val lastUsed = accountRepository.getLastUsedAccount()
            val initialAccount = lastUsed?.takeIf { it.email.isNotBlank() } ?: savedWithEmail.firstOrNull()

            setState {
                copy(
                    savedAccounts = savedWithEmail,
                    email = initialAccount?.email ?: email,
                    password = if (initialAccount?.rememberPassword == true) initialAccount.password else password,
                    rememberPassword = initialAccount?.rememberPassword ?: true
                )
            }
        }
    }

    override fun dispatch(action: LoginAction) {
        when (action) {
            is LoginAction.UpdateEmail -> handleUpdateEmail(action.email)
            is LoginAction.UpdatePassword -> setState { copy(password = action.password) }
            is LoginAction.UpdateCookies -> setState { copy(cookies = action.cookies) }
            is LoginAction.ToggleRememberPassword -> setState { copy(rememberPassword = action.remember) }
            is LoginAction.SelectSavedAccount -> handleSelectSavedAccount(action.account)
            LoginAction.TogglePasswordVisibility -> setState { copy(isPasswordVisible = !isPasswordVisible) }
            LoginAction.ToggleCookies -> setState { copy(isUsingCookies = !isUsingCookies) }
            LoginAction.Login -> doLogin()
        }
    }

    private fun handleUpdateEmail(newEmail: String) {
        val matched = state.value.savedAccounts.firstOrNull { it.email == newEmail }
        if (matched != null) {
            setState {
                copy(
                    email = newEmail,
                    password = if (matched.rememberPassword) matched.password else password,
                    rememberPassword = matched.rememberPassword
                )
            }
        } else {
            setState { copy(email = newEmail) }
        }
    }

    private fun handleSelectSavedAccount(account: Account) {
        setState {
            copy(
                email = account.email,
                password = if (account.rememberPassword) account.password else "",
                rememberPassword = account.rememberPassword
            )
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
                    saveAccountUseCase(
                        email = s.email,
                        password = s.password,
                        rememberPassword = s.rememberPassword
                    )
                }
                MainViewModel.invalidateAllCache()
                sendEffect(LoginEffect.ShowToast("登录成功"))
                sendEffect(LoginEffect.NavigateTo(Route.Main, Route.Splash))
            } catch (e: Exception) {
                sendEffect(LoginEffect.ShowToast(e.message ?: "登录失败请重试"))
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
