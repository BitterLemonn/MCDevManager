package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity

interface AccountRepository {

    suspend fun getAllAccounts(): List<AccountEntity>

    suspend fun getLastUsedAccount(): AccountEntity?

    suspend fun getAccountByNickname(nickname: String): AccountEntity?

    suspend fun upsertAccount(account: AccountEntity)

    suspend fun deleteAccount(id: Long)

    suspend fun updateNicknameById(id: Long, nickname: String)
}
