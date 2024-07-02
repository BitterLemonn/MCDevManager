package com.lemon.mcdevmanager.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lemon.mcdevmanager.data.database.entities.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM userEntity WHERE nickname = :nickname")
    fun getUserByNickname(nickname: String): UserEntity?

    @Query("SELECT * FROM userEntity")
    fun getAllUsers(): List<UserEntity>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateUser(user: UserEntity)

    @Query("DELETE FROM userEntity WHERE nickname = :nickname")
    fun deleteUserByNickname(nickname: String)
}