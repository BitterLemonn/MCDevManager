package com.lemon.mcdevmanagermp.domain.account

import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

interface AccountRepository {

    fun getAllAccounts(): Flow<List<AccountEntity>>

    suspend fun getLastUsedAccount(): AccountEntity?

    suspend fun getAccountByEmail(email: String): AccountEntity?

    suspend fun upsertAccount(account: AccountEntity)

    suspend fun deleteAccount(id: Long)
}
