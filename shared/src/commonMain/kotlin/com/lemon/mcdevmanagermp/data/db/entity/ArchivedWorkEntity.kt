package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.Entity

/**
 * 本地已归档作品记录，按账号 + 作品 ID 隔离存储。
 * accountKey 取当前登录开发者昵称，itemId 为作品 46id。
 */
@Entity(tableName = "archived_work", primaryKeys = ["accountKey", "itemId"])
data class ArchivedWorkEntity(
    val accountKey: String,
    val itemId: String,
    val archivedAt: Long
)
