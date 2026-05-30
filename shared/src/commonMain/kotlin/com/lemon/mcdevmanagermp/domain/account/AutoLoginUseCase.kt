package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.Logger

class AutoLoginUseCase(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(): Boolean {
        return try {
            val lastAccount = accountRepository.getLastUsedAccount()
            if (lastAccount != null) {
                val cookies: Map<String, String> =
                    JSONConverter.decodeFromString(lastAccount.cookiesJson)
                cookies.forEach { (k, v) -> AppContext.cookiesStore.addCookie(k, v) }
                val result = userRepository.getUserInfo()
                if (result is NetworkState.Success) {
                    Logger.d("获取账号信息成功 登录账号: ${lastAccount.email}")
                    return true
                }
                AppContext.cookiesStore.clearCookies()
            }
            false
        } catch (e: Exception) {
            Logger.d("自动登录检查失败: ${e.message}")
            AppContext.cookiesStore.clearCookies()
            false
        }
    }
}
