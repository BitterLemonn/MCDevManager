package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.common.JSONConverter
import com.lemon.mcdevmanagermp.data.common.NetworkState
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.domain.user.UserRepository
import kotlinx.serialization.serializer
import kotlin.time.Clock

class SaveAccountUseCase(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(email: String = "") {
        val cookies = AppContext.cookiesStore.getAllCookiesMap()
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
            accountRepository.upsertAccount(
                existing.copy(cookiesJson = cookiesJson, lastLoginTime = now, headImg = headImg)
            )
        } else {
            accountRepository.upsertAccount(
                AccountEntity(
                    nickname = accountName,
                    cookiesJson = cookiesJson,
                    lastLoginTime = now,
                    headImg = headImg
                )
            )
        }
    }
}
