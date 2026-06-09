package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.domain.account.AccountRepository

class AccountRepositoryImpl : AccountRepository {

    companion object {
        val INSTANCE by lazy { AccountRepositoryImpl() }
    }

    private val dao by lazy { AppContext.database.accountDao() }

    override suspend fun getAllAccounts(): List<AccountEntity> = dao.getAllAccounts()

    override suspend fun getLastUsedAccount(): AccountEntity? = dao.getLastUsedAccount()

    override suspend fun getAccountByEmail(email: String): AccountEntity? =
        dao.getAccountByEmail(email)

    override suspend fun upsertAccount(account: AccountEntity) = dao.upsertAccount(account)

    override suspend fun deleteAccount(id: Long) = dao.deleteAccount(id)
}
