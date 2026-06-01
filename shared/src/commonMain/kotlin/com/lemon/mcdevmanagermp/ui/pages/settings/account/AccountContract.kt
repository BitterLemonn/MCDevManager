package com.lemon.mcdevmanagermp.ui.pages.settings.account

import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.utils.extension.IUiAction
import com.lemon.mcdevmanagermp.utils.extension.IUiEffect
import com.lemon.mcdevmanagermp.utils.extension.IUiState

data class AccountState(
    val accounts: List<AccountEntity> = emptyList(),
    val currentAccountId: Long? = null,
    val isLoading: Boolean = false,
    val isSwitching: Long? = null,
    val showDeleteDialog: Boolean = false,
    val accountToDelete: AccountEntity? = null
) : IUiState

sealed interface AccountAction : IUiAction {
    data object LoadAccounts : AccountAction
    data class SwitchAccount(val account: AccountEntity) : AccountAction
    data class RequestDelete(val account: AccountEntity) : AccountAction
    data object ConfirmDelete : AccountAction
    data object DismissDelete : AccountAction
    data object Logout : AccountAction
}

sealed interface AccountEffect : IUiEffect {
    data class ShowToast(val message: String) : AccountEffect
    data object NavigateToLogin : AccountEffect
    data object AccountSwitched : AccountEffect
}
