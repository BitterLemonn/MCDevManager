package com.lemon.mcdevmanagermp.ui.pages.settings.account

import androidx.lifecycle.viewModelScope
import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.repository.AccountRepositoryImpl
import com.lemon.mcdevmanagermp.data.repository.UserRepositoryImpl
import com.lemon.mcdevmanagermp.domain.account.SaveAccountUseCase
import com.lemon.mcdevmanagermp.ui.base.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.time.Clock

class AccountViewModel : BaseViewModel<AccountState, AccountAction, AccountEffect>(AccountState()) {

    private val accountRepository = AccountRepositoryImpl.INSTANCE
    private val userRepository = UserRepositoryImpl.INSTANCE
    private val saveAccountUseCase = SaveAccountUseCase(
        accountRepository = accountRepository,
        userRepository = userRepository
    )

    init {
        loadAccounts()
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
            accountRepository.getAllAccounts().catch {
                sendEffect(AccountEffect.ShowToast("加载账号列表失败"))
            }.collect { accounts ->
                val lastUsed = accountRepository.getLastUsedAccount()
                setState {
                    copy(
                        accounts = accounts,
                        currentAccountId = lastUsed?.id
                    )
                }
            }
        }
    }

    private fun switchAccount(account: com.lemon.mcdevmanagermp.data.db.entity.AccountEntity) {
        setState { copy(isSwitching = account.id) }
        viewModelScope.launch {
            try {
                val cookies: Map<String, String> = JSONConverter.decodeFromString(account.cookiesJson)
                AppContext.cookiesStore.clearCookies()
                cookies.forEach { (k, v) -> AppContext.cookiesStore.addCookie(k, v) }
                val result = userRepository.getUserInfo()
                if (result is com.lemon.mcdevmanagermp.data.common.NetworkState.Success) {
                    val headImg = result.data?.headImg
                    accountRepository.upsertAccount(
                        account.copy(
                            lastLoginTime = Clock.System.now().toEpochMilliseconds(),
                            headImg = headImg
                        )
                    )
                    sendEffect(AccountEffect.ShowToast("已切换到 ${account.email}"))
                    sendEffect(AccountEffect.AccountSwitched)
                } else {
                    AppContext.cookiesStore.clearCookies()
                    sendEffect(AccountEffect.ShowToast("账号已过期，请重新登录"))
                    sendEffect(AccountEffect.NavigateToLogin)
                }
            } catch (e: Exception) {
                AppContext.cookiesStore.clearCookies()
                sendEffect(AccountEffect.ShowToast("切换失败: ${e.message}"))
            } finally {
                setState { copy(isSwitching = null) }
            }
        }
    }

    private fun confirmDelete() {
        val target = state.value.accountToDelete ?: return
        viewModelScope.launch {
            accountRepository.deleteAccount(target.id)
            setState { copy(showDeleteDialog = false, accountToDelete = null) }
            sendEffect(AccountEffect.ShowToast("已删除账号 ${target.email}"))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            val currentId = state.value.currentAccountId
            if (currentId != null) {
                accountRepository.deleteAccount(currentId)
            }
            AppContext.cookiesStore.clearCookies()
            sendEffect(AccountEffect.NavigateToLogin)
        }
    }
}
