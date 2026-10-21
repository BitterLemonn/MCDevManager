package com.lemon.mcdevmanagermp.data.repository

import com.lemon.mcdevmanagermp.data.common.AppContext
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity
import com.lemon.mcdevmanagermp.domain.account.Account
import com.lemon.mcdevmanagermp.domain.account.AccountRepository
import com.lemon.mcdevmanagermp.utils.Logger
import com.lemon.mcdevmanagermp.utils.PasswordCrypto
import kotlinx.coroutines.CancellationException

class AccountRepositoryImpl : AccountRepository {

    companion object {
        val INSTANCE by lazy { AccountRepositoryImpl() }
    }

    private val dao by lazy { AppContext.database.accountDao() }

    override suspend fun getAllAccounts(): List<Account> =
        dao.getAllAccounts().map { it.toAccount().encryptLegacyPassword() }

    override suspend fun getLastUsedAccount(): Account? =
        dao.getLastUsedAccount()?.toAccount()?.encryptLegacyPassword()

    override suspend fun getAccountByNickname(nickname: String): Account? =
        dao.getAccountByNickname(nickname)?.toAccount()?.encryptLegacyPassword()

    override suspend fun upsertAccount(account: Account) =
        dao.upsertAccount(account.toEntity())

    override suspend fun deleteAccount(id: Long) = dao.deleteAccount(id)

    override suspend fun updateNicknameById(id: Long, nickname: String) =
        dao.updateNicknameById(id, nickname)

    /**
     * 历史版本将密码以明文写入数据库，读取时转换为密文并回写，避免明文长期留存。
     */
    private suspend fun Account.encryptLegacyPassword(): Account {
        if (password.isEmpty() || PasswordCrypto.isEncrypted(password)) return this
        return try {
            val encrypted = copy(password = PasswordCrypto.encrypt(password))
            dao.upsertAccount(encrypted.toEntity())
            encrypted
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("密码加密回写失败: ${e.message}")
            this
        }
    }

    private fun AccountEntity.toAccount() = Account(
        id = id,
        nickname = nickname,
        cookiesJson = cookiesJson,
        lastLoginTime = lastLoginTime,
        headImg = headImg,
        email = email,
        password = password,
        rememberPassword = rememberPassword
    )

    private fun Account.toEntity() = AccountEntity(
        id = id,
        nickname = nickname,
        cookiesJson = cookiesJson,
        lastLoginTime = lastLoginTime,
        headImg = headImg,
        email = email,
        password = password,
        rememberPassword = rememberPassword
    )
}
