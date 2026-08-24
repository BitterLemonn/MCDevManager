package com.lemon.mcdevmanagermp.ui.pages.login

import com.lemon.mcdevmanagermp.domain.account.Account
import com.lemon.mcdevmanagermp.ui.navigation.Route
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class LoginState(
    val email: String = "",
    val password: String = "",
    val cookies: String = "",
    val rememberPassword: Boolean = true,
    val savedAccounts: List<Account> = emptyList(),
    val isUsingCookies: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
) : IUiState

sealed interface LoginAction : IUiAction {
    data class UpdateEmail(val email: String) : LoginAction
    data class UpdatePassword(val password: String) : LoginAction
    data class UpdateCookies(val cookies: String) : LoginAction
    data class ToggleRememberPassword(val remember: Boolean) : LoginAction
    data class SelectSavedAccount(val account: Account) : LoginAction
    data object TogglePasswordVisibility : LoginAction
    data object ToggleCookies : LoginAction
    data object Login : LoginAction
}

sealed interface LoginEffect : IUiEffect {
    data class ShowToast(val message: String) : LoginEffect
    data class NavigateTo(val route: Route, val popUpTo: Route? = null) : LoginEffect
}
