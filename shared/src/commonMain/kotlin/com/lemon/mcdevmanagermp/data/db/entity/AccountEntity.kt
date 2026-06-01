package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val cookiesJson: String,
    val lastLoginTime: Long,
    val headImg: String? = null,
)
