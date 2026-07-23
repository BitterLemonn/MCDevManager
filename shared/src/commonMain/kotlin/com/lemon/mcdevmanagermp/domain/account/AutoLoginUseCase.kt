package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.consts.CookiesExpiredException
import com.lemon.mcdevmanagermp.data.consts.LoginException
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import com.lemon.mcdevmanagermp.utils.Logger
import kotlin.time.Clock

class AutoLoginUseCase(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val cookieRepository: CookieRepository
) {

    suspend operator fun invoke(): Boolean {
        return try {
            val lastAccount = accountRepository.getLastUsedAccount()
            if (lastAccount != null) {
                val cookies: Map<String, String> =
                    JSONConverter.decodeFromString(lastAccount.cookiesJson)
                cookies.forEach { (k, v) -> cookieRepository.addCookie(k, v) }
                val result = userRepository.getUserInfo()
                if (result is NetworkState.Success) {
                    val userInfo = result.data
                    val now = Clock.System.now().toEpochMilliseconds()
                    // 更新最后登录时间，同时兼容旧版本将 email 字段更新为 nickname
                    val nickname = userInfo?.nickname
                    if (nickname != null && nickname != lastAccount.nickname) {
                        accountRepository.upsertAccount(
                            lastAccount.copy(
                                nickname = nickname,
                                lastLoginTime = now,
                                headImg = userInfo.headImg
                            )
                        )
                    } else {
                        accountRepository.upsertAccount(
                            lastAccount.copy(lastLoginTime = now)
                        )
                    }
                    Logger.d("获取账号信息成功 登录账号: ${lastAccount.nickname}")
                    return true
                }
                // 仅明确判定登录过期才清理；网络/服务异常乐观放行进 Main，
                // 避免冷启动网络抖动误判过期（真过期由 Main 层 isSessionExpired 兜底）
                val expired = result is NetworkState.Error &&
                        (result.e is LoginException || result.e is CookiesExpiredException)
                if (expired) cookieRepository.clearCookies()
                return !expired
            }
            false
        } catch (e: Exception) {
            Logger.d("自动登录检查失败: ${e.message}")
            cookieRepository.clearCookies()
            false
        }
    }
}
