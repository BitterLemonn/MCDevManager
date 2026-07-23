package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.Entity

/**
 * 数据追踪页「上次查询配置」，按账号 + 平台隔离。
 * accountKey 取当前登录开发者昵称；selectedIIDs 为逗号分隔的资源 IID 串。
 */
@Entity(tableName = "day_detail_config", primaryKeys = ["accountKey", "platform"])
data class DayDetailConfigEntity(
    val accountKey: String,
    val platform: String,
    val dateSpanDays: Int,
    val selectedIIDs: String,
    val updatedAt: Long
)
