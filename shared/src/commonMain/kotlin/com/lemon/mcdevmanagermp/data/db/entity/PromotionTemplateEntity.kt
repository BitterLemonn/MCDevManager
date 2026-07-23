package com.lemon.mcdevmanagermp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PE 轮播图申请文案模板。
 * 字段 updateContent 列名避开 SQL 保留字 UPDATE（domain 层仍称 update）。
 */
@Entity(tableName = "promotion_template")
data class PromotionTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val extra: String,
    val activity: String,
    val feature: String,
    val updateContent: String,
    val promoImageUrl: String = "",
    val createdAt: Long
)
