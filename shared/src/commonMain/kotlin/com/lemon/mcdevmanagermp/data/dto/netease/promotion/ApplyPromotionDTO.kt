package com.lemon.mcdevmanagermp.data.dto.netease.promotion

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplyPromotionDTO(
    @SerialName("position_id")
    val positionId: String,
    @SerialName("start_time")
    val startTime: Long,
    @SerialName("item_id")
    val itemId: String,
    @SerialName("item_category")
    val itemCategory: String,
    val extra: String,
    val activity: String,
    val feature: String,
    val update: String,
    @SerialName("pe_channel")
    val peChannel: String,
    @SerialName("special_promotion")
    val specialPromotion: Boolean,
    @SerialName("start_time_zone")
    val startTimeZone: Int,
    @SerialName("is_check")
    val isCheck: Boolean
)
