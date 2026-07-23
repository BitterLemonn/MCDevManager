package com.lemon.mcdevmanagermp.data.dto.netease.activity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CancelJoinDiscountDTO(
    @SerialName("activity_id")
    val activityId: String,
    @SerialName("item_id")
    val itemId: String
)