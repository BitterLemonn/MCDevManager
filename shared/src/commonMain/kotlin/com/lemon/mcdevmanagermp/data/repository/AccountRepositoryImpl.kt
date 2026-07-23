package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.domain.account.Account
import com.lemon.mcdevmanagermp.domain.account.AccountRepository

class AccountRepositoryImpl : AccountRepository {

    companion object {
        val INSTANCE by lazy { AccountRepositoryImpl() }
    }

    private val dao by lazy { AppContext.database.accountDao() }

    override suspend fun getAllAccounts(): List<Account> =
        dao.getAllAccounts().map { it.toAccount() }

    override suspend fun getLastUsedAccount(): Account? =
        dao.getLastUsedAccount()?.toAccount()

    override suspend fun getAccountByNickname(nickname: String): Account? =
        dao.getAccountByNickname(nickname)?.toAccount()

    override suspend fun upsertAccount(account: Account) =
        dao.upsertAccount(account.toEntity())

    override suspend fun deleteAccount(id: Long) = dao.deleteAccount(id)

    override suspend fun updateNicknameById(id: Long, nickname: String) =
        dao.updateNicknameById(id, nickname)

    private fun AccountEntity.toAccount() = Account(
        id = id,
        nickname = nickname,
        cookiesJson = cookiesJson,
        lastLoginTime = lastLoginTime,
        headImg = headImg
    )

    private fun Account.toEntity() = AccountEntity(
        id = id,
        nickname = nickname,
        cookiesJson = cookiesJson,
        lastLoginTime = lastLoginTime,
        headImg = headImg
    )
}
