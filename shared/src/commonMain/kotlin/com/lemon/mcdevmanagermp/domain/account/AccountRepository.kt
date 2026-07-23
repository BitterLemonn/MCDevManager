package com.lemon.mcdevmanagermp.domain.account

interface AccountRepository {

    suspend fun getAllAccounts(): List<Account>

    suspend fun getLastUsedAccount(): Account?

    suspend fun getAccountByNickname(nickname: String): Account?

    suspend fun upsertAccount(account: Account)

    suspend fun deleteAccount(id: Long)

    suspend fun updateNicknameById(id: Long, nickname: String)
}
