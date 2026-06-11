package com.lemon.mcdevmanagermp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lemon.mcdevmanagermp.data.db.entity.AccountEntity

@Dao
interface AccountDao {

    @Query("SELECT * FROM account ORDER BY lastLoginTime DESC")
    suspend fun getAllAccounts(): List<AccountEntity>

    @Query("SELECT * FROM account ORDER BY lastLoginTime DESC LIMIT 1")
    suspend fun getLastUsedAccount(): AccountEntity?

    @Query("SELECT * FROM account WHERE nickname = :nickname LIMIT 1")
    suspend fun getAccountByNickname(nickname: String): AccountEntity?

    @Upsert
    suspend fun upsertAccount(account: AccountEntity)

    @Query("DELETE FROM account WHERE id = :id")
    suspend fun deleteAccount(id: Long)

    @Query("UPDATE account SET nickname = :nickname WHERE id = :id")
    suspend fun updateNicknameById(id: Long, nickname: String)
}
