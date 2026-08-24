package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import kotlinx.serialization.serializer
import kotlin.time.Clock

class SaveAccountUseCase(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val cookieRepository: CookieRepository
) {

    suspend operator fun invoke(
        email: String = "",
        password: String = "",
        rememberPassword: Boolean = false
    ) {
        val cookies = cookieRepository.getAllCookiesMap()
        val cookiesJson = JSONConverter.encodeToString(serializer<Map<String, String>>(), cookies)
        val now = Clock.System.now().toEpochMilliseconds()
        val userInfo = runCatching {
            val result = userRepository.getUserInfo()
            (result as? NetworkState.Success)?.data
        }.getOrNull()
        // 统一使用 nickname 作为账号标识，避免邮箱登录和 cookies 登录产生重复账号
        val accountName = userInfo?.nickname ?: email.ifBlank { "" }
        val headImg = userInfo?.headImg
        val existing = accountRepository.getAccountByNickname(accountName)
        if (existing != null) {
            val savedEmail = if (email.isNotBlank()) email else existing.email
            val (savedPassword, savedRemember) = if (email.isNotBlank()) {
                if (rememberPassword) password to true else "" to false
            } else {
                existing.password to existing.rememberPassword
            }
            accountRepository.upsertAccount(
                existing.copy(
                    cookiesJson = cookiesJson,
                    lastLoginTime = now,
                    headImg = headImg,
                    email = savedEmail,
                    password = savedPassword,
                    rememberPassword = savedRemember
                )
            )
        } else {
            val savedPassword = if (rememberPassword) password else ""
            accountRepository.upsertAccount(
                Account(
                    nickname = accountName,
                    cookiesJson = cookiesJson,
                    lastLoginTime = now,
                    headImg = headImg,
                    email = email,
                    password = savedPassword,
                    rememberPassword = rememberPassword
                )
            )
        }
    }
}
