package com.lemon.mcdevmanager.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val nickname: String,
    @ColumnInfo val cookie: String,
    @ColumnInfo val username: String,
    @ColumnInfo val password: String
)