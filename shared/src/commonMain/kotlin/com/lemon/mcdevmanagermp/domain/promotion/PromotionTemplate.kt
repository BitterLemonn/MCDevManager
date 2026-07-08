package com.lemon.mcdevmanagermp.domain.promotion

/**
 * PE 轮播图申请文案模板：保存历史提交的 4 个 HTML 描述字段，供一键套用。
 */
data class PromotionTemplate(
    val id: Long = 0,
    val name: String,
    val extra: String,
    val activity: String,
    val feature: String,
    val update: String,
    val promoImageUrl: String = "",
    val createdAt: Long
)
