package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import kotlin.time.Clock

/**
 * 账号管理 UseCase：封装账号列表加载、切换、删除等业务逻辑
 */
class AccountManageUseCase(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) {
    /**
     * 获取所有账号及最后使用的账号信息
     */
    suspend fun getAccountsWithLastUsed(): AccountListResult {
        val accounts = accountRepository.getAllAccounts()
        val lastUsed = accountRepository.getLastUsedAccount()

        return AccountListResult(accounts, lastUsed?.id)
    }

    /**
     * 切换账号：恢复 cookies → 验证有效性 → 更新最后登录时间
     * @return 切换结果
     */
    suspend fun switchAccount(account: AccountEntity): SwitchResult {
        val cookies: Map<String, String> = JSONConverter.decodeFromString(account.cookiesJson)
        AppContext.cookiesStore.clearCookies()
        cookies.forEach { (k, v) -> AppContext.cookiesStore.addCookie(k, v) }

        val result = userRepository.getUserInfo()
        return if (result is NetworkState.Success) {
            val headImg = result.data?.headImg
            accountRepository.upsertAccount(
                account.copy(
                    lastLoginTime = Clock.System.now().toEpochMilliseconds(),
                    headImg = headImg
                )
            )
            SwitchResult.Success(account.email)
        } else {
            AppContext.cookiesStore.clearCookies()
            SwitchResult.Expired
        }
    }

    /**
     * 删除指定账号
     */
    suspend fun deleteAccount(accountId: Long) {
        accountRepository.deleteAccount(accountId)
    }

    sealed class SwitchResult {
        data class Success(val email: String) : SwitchResult()
        data object Expired : SwitchResult()
    }
}

data class AccountListResult(
    val accounts: List<AccountEntity>,
    val currentAccountId: Long?
)
