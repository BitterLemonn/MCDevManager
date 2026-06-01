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

    suspend operator fun invoke(email: String) {
        val cookies = AppContext.cookiesStore.getAllCookiesMap()
        val cookiesJson = JSONConverter.encodeToString(serializer<Map<String, String>>(), cookies)
        val now = Clock.System.now().toEpochMilliseconds()
        val userInfo = runCatching {
            val result = userRepository.getUserInfo()
            (result as? NetworkState.Success)?.data
        }.getOrNull()
        val accountEmail = email.ifBlank { userInfo?.nickname ?: "" }
        val headImg = userInfo?.headImg
        val existing = accountRepository.getAccountByEmail(accountEmail)
        if (existing != null) {
            accountRepository.upsertAccount(
                existing.copy(cookiesJson = cookiesJson, lastLoginTime = now, headImg = headImg)
            )
        } else {
            accountRepository.upsertAccount(
                AccountEntity(
                    email = accountEmail,
                    cookiesJson = cookiesJson,
                    lastLoginTime = now,
                    headImg = headImg
                )
            )
        }
    }
}
