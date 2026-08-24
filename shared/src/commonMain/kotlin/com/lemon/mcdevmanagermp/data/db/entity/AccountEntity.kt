package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nickname: String,
    val cookiesJson: String,
    val lastLoginTime: Long,
    val headImg: String? = null,
    @ColumnInfo(defaultValue = "")
    val email: String = "",
    @ColumnInfo(defaultValue = "")
    val password: String = "",
    @ColumnInfo(defaultValue = "0")
    val rememberPassword: Boolean = false,
)
