package com.lemon.mcdevmanagermp.ui.pages.settings.account

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.repository.AccountRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.CookieRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.account.Account
import com.lemon.mcdevmanagermp.domain.account.AccountManageUseCase
import com.lemon.mcdevmanagermp.domain.account.SaveAccountUseCase
import com.lemon.mcdevmanagermp.domain.user.GetLevelInfoUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import com.lemon.mcdevmanagermp.utils.Logger
import kotlinx.coroutines.launch

class AccountViewModel : BaseViewModel<AccountState, AccountAction, AccountEffect>(AccountState()) {

    private val cookieRepository = CookieRepositoryImpl.INSTANCE
    private val accountManageUseCase = AccountManageUseCase(
        accountRepository = AccountRepositoryImpl.INSTANCE,
        userRepository = UserRepositoryImpl.INSTANCE,
        cookieRepository = cookieRepository
    )
    private val saveAccountUseCase = SaveAccountUseCase(
        accountRepository = AccountRepositoryImpl.INSTANCE,
        userRepository = UserRepositoryImpl.INSTANCE,
        cookieRepository = cookieRepository
    )
    private val getLevelInfoUseCase = GetLevelInfoUseCase(
        userRepository = UserRepositoryImpl.INSTANCE
    )

    init {
        loadAccounts()
        loadLevelInfo()
    }

    override fun dispatch(action: AccountAction) {
        when (action) {
            AccountAction.LoadAccounts -> loadAccounts()
            is AccountAction.SwitchAccount -> switchAccount(action.account)
            is AccountAction.RequestDelete -> setState {
                copy(showDeleteDialog = true, accountToDelete = action.account)
            }

            AccountAction.ConfirmDelete -> confirmDelete()
            AccountAction.DismissDelete -> setState {
                copy(showDeleteDialog = false, accountToDelete = null)
            }

            AccountAction.Logout -> logout()
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            try {
                val result = accountManageUseCase.getAccountsWithLastUsed()
                setState {
                    copy(
                        accounts = result.accounts,
                        currentAccountId = result.currentAccountId
                    )
                }
            } catch (e: Exception) {
                Logger.e("加载账号列表失败: $e")
                sendEffect(AccountEffect.ShowToast("加载账号列表失败"))
            }
        }
    }

    private fun switchAccount(account: Account) {
        setState { copy(isSwitching = account.id) }
        viewModelScope.launch {
            try {
                when (val result = accountManageUseCase.switchAccount(account)) {
                    is AccountManageUseCase.SwitchResult.Success -> {
                        // 重新加载账号列表以更新 currentAccountId 和账号信息
                        loadAccounts()
                        loadLevelInfo()
                        Logger.d("已切换到 ${result.nickname}")
                        sendEffect(AccountEffect.ShowToast("已切换到 ${result.nickname}"))
                        sendEffect(AccountEffect.AccountSwitched)
                    }

                    is AccountManageUseCase.SwitchResult.Expired -> {
                        Logger.e("账号已过期, 请重新登录")
                        sendEffect(AccountEffect.ShowToast("账号已过期，请重新登录"))
                        sendEffect(AccountEffect.NavigateToLogin)
                    }
                }
            } catch (e: Exception) {
                cookieRepository.clearCookies()
                sendEffect(AccountEffect.ShowToast("切换失败: ${e.message}"))
                Logger.e("切换账号失败: $e")
            } finally {
                setState { copy(isSwitching = null) }
            }
        }
    }

    private fun confirmDelete() {
        val target = state.value.accountToDelete ?: return
        viewModelScope.launch {
            accountManageUseCase.deleteAccount(target.id)
            setState { copy(showDeleteDialog = false, accountToDelete = null) }
            // 重新加载账号列表以刷新 UI
            loadAccounts()
            sendEffect(AccountEffect.ShowToast("已删除账号 ${target.nickname}"))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            val currentId = state.value.currentAccountId
            if (currentId != null) {
                accountManageUseCase.deleteAccount(currentId)
            }
            cookieRepository.clearCookies()
            sendEffect(AccountEffect.NavigateToLogin)
        }
    }

    private fun loadLevelInfo() {
        viewModelScope.launch {
            setState { copy(isLoadingLevel = true) }
            try {
                when (val result = getLevelInfoUseCase()) {
                    is NetworkState.Success -> setState {
                        copy(levelInfo = result.data, isLoadingLevel = false)
                    }

                    is NetworkState.Error -> {
                        setState { copy(isLoadingLevel = false) }
                        // session 过期/未登录时不阻断账号管理，静默降级
                        Logger.e("加载等级信息失败: ${result.e}")
                    }
                }
            } catch (e: Exception) {
                setState { copy(isLoadingLevel = false) }
                Logger.e("加载等级信息异常: $e")
            }
        }
    }
}
